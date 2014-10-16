// IECanvas.cpp : Defines the entry point for the DLL application.
//

#include "stdafx.h"
#include "IECanvas.h"
#include "IE4Events.h"
#include "Registry.h"
#include "objbase.h"

#include "jawt_md.h"

#include "com_craspp_browser_IECanvas.h"

const char* CLSID_MozillaBrowserString = "{1339B54C-3453-11D2-93B9-000000000000}";

/*BOOL APIENTRY DllMain(HANDLE hModule,
                       DWORD  ul_reason_for_call,
                       LPVOID lpReserved
					 )
{

    return TRUE;
}*/

#define MSG_NAVIGATE2	(WM_USER+1)
#define MSG_RESIZE		(WM_USER+2)
#define MSG_REFRESH		(WM_USER+3)
#define MSG_GOHOME		(WM_USER+4)
#define MSG_GOFORWARD	(WM_USER+5)
#define MSG_GOBACK		(WM_USER+6)
#define MSG_SETFOCUS	(WM_USER+7)


CIECanvas::CIECanvas(JavaVM *pJavaVM, jobject pObj):
	m_pJavaVM(pJavaVM), m_pObj(pObj), m_dwCookie(0) {

    AtlAxWinInit();
	m_pIE4Events = new CIE4Events(this, pJavaVM, pObj);
}

CIECanvas::~CIECanvas() {

}

BOOL CIECanvas::Initialize(HWND hwnd, int tryMozilla) {

	hwndParent = hwnd;

	if (tryMozilla) { // Initialize using mozilla..

		hwndChild = ::CreateWindow("AtlAxWin", CLSID_MozillaBrowserString,
			WS_CHILD|WS_VISIBLE|WS_TABSTOP, 0,0,0,0, hwnd, NULL,
			::GetModuleHandle(NULL), NULL);

		// We should try to fallback to IE if mozilla not available...

	} else { // Initialize with IE5

		hwndChild = ::CreateWindow("AtlAxWin", "Shell.Explorer.1",
			WS_CHILD|WS_VISIBLE|WS_TABSTOP, 0,0,0,0, hwnd, NULL,
			::GetModuleHandle(NULL), NULL);
	}

    IUnknown *pUnk = NULL;
    AtlAxGetControl(hwndChild, &pUnk);

    pUnk->QueryInterface(IID_IWebBrowser2, (void**)&m_spBrowser);

    if (m_spBrowser) {
        m_spBrowser->put_Visible(VARIANT_TRUE);

		// Set up the event sink
        BOOL bAdvised = AfxConnectionAdvise(pUnk /*m_spBrowser*/, DIID_DWebBrowserEvents2,
                                            m_pIE4Events->GetInterface(&IID_IUnknown),
                                            TRUE, &m_dwCookie);

		m_spBrowser->Refresh();
		return TRUE;
	}
	return FALSE;
}

void CIECanvas::UnInitialize() {
	/*if (m_spBrowser)
		AtlUnadvise(m_spBrowser, DIID_DWebBrowserEvents2, m_dwCookie);	*/
	return;
}

void CIECanvas::Navigate2(char *pURL) {
    if (m_spBrowser) {
        CComVariant ve;
        CComVariant vurl(pURL);
#pragma warning(disable: 4310) // cast truncates constant value
        m_spBrowser->put_Visible(VARIANT_TRUE);
#pragma warning(default: 4310) // cast truncates constant value
        m_spBrowser->Navigate2(&vurl, &ve, &ve, &ve, &ve);
    }
}

void CIECanvas::Refresh() {
	if (m_spBrowser) {
        m_spBrowser->put_Visible(VARIANT_TRUE);
        m_spBrowser->Refresh();
    }
}

void CIECanvas::GoForward() {
	if (m_spBrowser) {
        m_spBrowser->put_Visible(VARIANT_TRUE);
        m_spBrowser->GoForward();
    }
}

void CIECanvas::GoBack() {
	if (m_spBrowser) {
        m_spBrowser->put_Visible(VARIANT_TRUE);
        m_spBrowser->GoBack();
    }
}


void CIECanvas::GoHome() {
	if (m_spBrowser) {
        m_spBrowser->put_Visible(VARIANT_TRUE);
        m_spBrowser->GoHome();
    }
}

HWND CIECanvas::GetHWNDChild() {
	return hwndChild;
}

HWND CIECanvas::GetHWNDParent() {
	return hwndParent;
}


void CIECanvas::UpdateUIHandler() {
	CComPtr<IDispatch>		m_spDoc;

	if (m_spBrowser)
		m_spBrowser->get_Document(&m_spDoc);

    if (m_spDoc) {
		// make shure this really is a HTML document
		CComQIPtr<IHTMLDocument2, &IID_IHTMLDocument2> spHTML(m_spDoc);
		CComQIPtr<ICustomDoc, &IID_ICustomDoc> spCustomDoc(m_spDoc);

		if (spHTML && spCustomDoc) {
			spCustomDoc->SetUIHandler(m_pIE4Events);
			//printf("setting new UIHandler on document..\n");
		}
    }
}

void CIECanvas::AddEventToList(CString strEvent) {
	//printf("win: got event: %s\n", strEvent);
}




// IECanvas native methods interface implementation.

typedef struct {
	char szURL[1024];
	HWND hwnd;
	jint hashCode;
	boolean bUseMozilla;
	jobject pObjRef;
	JavaVM *pJavaVM;
} ThreadParam;

// forward..
static void WINAPIV runATL(LPVOID);

JNIEXPORT void JNICALL Java_com_craspp_browser_IECanvas_initialize(JNIEnv *pEnv, jobject pCanvas, jboolean bUseMozilla) {

	HWND hwndIn = 0;

	// get the AWT version
	JAWT awt;
	JAWT_DrawingSurface* ds;
	JAWT_DrawingSurfaceInfo* dsi;
	JAWT_Win32DrawingSurfaceInfo* dsi_win;
	jboolean result;
	jint lock;

	// find awt version
	awt.version = JAWT_VERSION_1_3;
	result = JAWT_GetAWT(pEnv, &awt);

	if (result == NULL) {
		printf("Needs AWT version 1.3 or newer!\n"); return; }
	ds = awt.GetDrawingSurface(pEnv, pCanvas);
	if (ds == NULL) {
		printf("unable to get drawing surface!\n"); return; }
	lock = ds->Lock(ds);
	if ((lock & JAWT_LOCK_ERROR) != 0) {
		printf("unable to lock drawing surface!\n"); return; }

	dsi = ds->GetDrawingSurfaceInfo(ds);
	dsi_win = (JAWT_Win32DrawingSurfaceInfo*)dsi->platformInfo;

	// get hwnd!
	//printf("the hwnd returned from jawt; [0x%x]\n", dsi_win->hwnd);
	hwndIn = dsi_win->hwnd;

	ds->FreeDrawingSurfaceInfo(dsi);
	ds->Unlock(ds);
	awt.FreeDrawingSurface(ds);
	// done!

	if (hwndIn == 0) {
		printf("Unable to get HWND to parent Canvas, aborting!\n"); return; }

	printf("using mozilla? %d", bUseMozilla);

	// Start new thread
	ThreadParam *pThreadParam = new ThreadParam;
	pEnv->GetJavaVM(&(pThreadParam->pJavaVM));

	// Lock the jobject referance so we can call java code safely.
	pThreadParam->pObjRef = pEnv->NewGlobalRef(pCanvas);

	pThreadParam->hashCode = Registry::GetHashCode(pEnv, pThreadParam->pObjRef);
	CIECanvas *pNull = NULL;

	// Sometimes the resize call is sent before the canvas is created.
	Registry::Add((void*)pThreadParam->hashCode, (void*&) pNull);

	pThreadParam->hwnd = (HWND) hwndIn;
	pThreadParam->bUseMozilla = bUseMozilla;

	if (pThreadParam->pObjRef == 0) {
		printf("error when doing global ref to obj..\n"); return; }

	_beginthread(runATL, 0, pThreadParam);
}

void WINAPIV runATL(LPVOID lpVoid) {

	ThreadParam *pThreadParam = (ThreadParam*) lpVoid;

	HWND hwnd = pThreadParam->hwnd;
    printf("Create AtlAxWin Begin...[0x%x]\n", pThreadParam->hwnd);

	CIECanvas* pIECanvas = new CIECanvas(pThreadParam->pJavaVM, pThreadParam->pObjRef);
	Registry::Add((void*)pThreadParam->hashCode, (void*&) pIECanvas);
	printf("using CIECanvas         [0x%x]\n", pIECanvas);
	printf("using mozilla? %d", pThreadParam->bUseMozilla);

	pIECanvas->Initialize((HWND)pThreadParam->hwnd, pThreadParam->bUseMozilla);

	delete pThreadParam;

	// Event message loop.

    MSG msg;
    while(GetMessage(&msg, NULL, 0, 0)) {
		TranslateMessage(&msg);
		DispatchMessage(&msg);
		/*
		 * This will cause unexpected crashes. Better way is to handle these
		 * in the corresponding methods.
		if (msg.message == MSG_NAVIGATE2) {
			ThreadParam *pParam = (ThreadParam*) msg.lParam;
			pIECanvas->Navigate2(pParam->szURL);
			delete pThreadParam;

		} else if (msg.message == MSG_RESIZE) {
		    RECT rc;
			::GetWindowRect(pIECanvas->GetHWNDParent(), &rc);
			::SetWindowPos(pIECanvas->GetHWNDChild(), NULL, 0, 0,
				rc.right-rc.left, rc.bottom-rc.top,
				SWP_NOZORDER|SWP_NOACTIVATE|SWP_SHOWWINDOW|SWP_NOMOVE);

		} else if (msg.message == MSG_REFRESH) {
			pIECanvas->Refresh();
		} else if (msg.message == MSG_GOHOME) {
			pIECanvas->GoHome();
		} else if (msg.message == MSG_GOFORWARD) {
			pIECanvas->GoForward();
		} else if (msg.message == MSG_GOBACK) {
			pIECanvas->GoBack();
		} else if (msg.message == MSG_SETFOCUS) {
			printf("focus hack!\n");
			SetFocus(GetParent(GetParent(pIECanvas->GetHWNDChild())));
		}
		*
		*
		*/
    }
}


JNIEXPORT void JNICALL Java_com_craspp_browser_IECanvas_sendurl(JNIEnv *pEnv, jobject pObj, jstring string)
{
	CIECanvas* pIECanvas;

    const char *str	= pEnv->GetStringUTFChars(string, 0);
	ThreadParam *pThreadParam = new ThreadParam;
    strcpy(pThreadParam->szURL,str);
    pEnv->ReleaseStringUTFChars(string, str);

	jint hashCode = Registry::GetHashCode(pEnv, pObj);
	Registry::Lookup((void*)hashCode, (void*&)pIECanvas);
	if (pIECanvas != NULL)
		//PostMessage(pIECanvas->GetHWNDChild(), MSG_NAVIGATE2, 0, (long)pThreadParam);
		pIECanvas->Navigate2(pThreadParam->szURL);

	/**
	  This shit won't work in this context

	printf("Setting new url...\n");
    const char *str	= pEnv->GetStringUTFChars(string, 0);
	ThreadParam *pThreadParam = new ThreadParam;
    strcpy(pThreadParam->szURL,str);
    pEnv->ReleaseStringUTFChars(string, str);
	HWND hwnd = (HWND) pThreadParam->hwnd;
    RECT rc;
    if(hwnd!=NULL)
    {
        ::GetWindowRect(hwnd,&rc);
        HWND hwndChild = GetWindow(hwnd, GW_CHILD);

		IUnknown *pUnk = NULL;
		AtlAxGetControl(hwndChild,&pUnk);
		// get an interface to set the URL.
		CComPtr<IWebBrowser2> spBrowser;
		pUnk->QueryInterface(IID_IWebBrowser2, (void**)&spBrowser);
		printf("About to navigate to %s\n", pThreadParam->szURL);
		if (spBrowser)
		{
			printf("About to navigate to %s\n", pThreadParam->szURL);
			CComVariant ve;
			CComVariant vurl(pThreadParam->szURL);
#pragma warning(disable: 4310) // cast truncates constant value
			spBrowser->put_Visible(VARIANT_TRUE);
#pragma warning(default: 4310) // cast truncates constant value
			spBrowser->Navigate2(&vurl, &ve, &ve, &ve, &ve);
		}
	}
	*/
}


JNIEXPORT void JNICALL Java_com_craspp_browser_IECanvas_resizeControl(JNIEnv *pEnv, jobject pObj) {
	CIECanvas* pIECanvas;

	jint hashCode = Registry::GetHashCode(pEnv, pObj);
	Registry::Lookup((void*)hashCode, (void*&)pIECanvas);

	/*
	if (pIECanvas != NULL)
		PostMessage(pIECanvas->GetHWNDChild(), MSG_RESIZE, 0, 0);
	*/
	if (pIECanvas != NULL)
	{
		RECT rc;
		::GetWindowRect(pIECanvas->GetHWNDParent(), &rc);
		::SetWindowPos(pIECanvas->GetHWNDChild(), NULL, 0, 0,
			rc.right-rc.left, rc.bottom-rc.top,
			SWP_NOZORDER|SWP_NOACTIVATE|SWP_SHOWWINDOW|SWP_NOMOVE);
	}

}

JNIEXPORT void JNICALL Java_com_craspp_browser_IECanvas_gohome(JNIEnv *pEnv, jobject pObj) {
	CIECanvas* pIECanvas;
	jint hashCode = Registry::GetHashCode(pEnv, pObj);
	Registry::Lookup((void*)hashCode, (void*&)pIECanvas);
	if (pIECanvas != NULL)
		//PostMessage(pIECanvas->GetHWNDChild(), MSG_GOHOME, 0, 0);
		pIECanvas->GoHome();
}

JNIEXPORT void JNICALL Java_com_craspp_browser_IECanvas_rld(JNIEnv *pEnv, jobject pObj) {
	CIECanvas* pIECanvas;
	jint hashCode = Registry::GetHashCode(pEnv, pObj);
	Registry::Lookup((void*)hashCode, (void*&)pIECanvas);

	if (pIECanvas != NULL)
		//PostMessage(pIECanvas->GetHWNDChild(), MSG_REFRESH, 0, 0);
		pIECanvas->Refresh();
}

JNIEXPORT void JNICALL Java_com_craspp_browser_IECanvas_gofwd(JNIEnv *pEnv, jobject pObj) {
	CIECanvas* pIECanvas;
	jint hashCode = Registry::GetHashCode(pEnv, pObj);
	Registry::Lookup((void*)hashCode, (void*&)pIECanvas);

	if (pIECanvas != NULL)
		//PostMessage(pIECanvas->GetHWNDChild(), MSG_GOFORWARD, 0, 0);
		pIECanvas->GoForward();
}


JNIEXPORT void JNICALL Java_com_craspp_browser_IECanvas_gobackwd(JNIEnv *pEnv, jobject pObj) {
	CIECanvas* pIECanvas;
	jint hashCode = Registry::GetHashCode(pEnv, pObj);
	Registry::Lookup((void*)hashCode, (void*&)pIECanvas);

	if (pIECanvas != NULL)
		//PostMessage(pIECanvas->GetHWNDChild(), MSG_GOBACK, 0, 0);
		pIECanvas->GoBack();
}


JNIEXPORT void JNICALL Java_com_craspp_browser_IECanvas_setfocus(JNIEnv *pEnv, jobject pObj) {
	CIECanvas* pIECanvas;
	jint hashCode = Registry::GetHashCode(pEnv, pObj);
	Registry::Lookup((void*)hashCode, (void*&)pIECanvas);

	if (pIECanvas != NULL)
		PostMessage(pIECanvas->GetHWNDChild(), MSG_SETFOCUS, 0, 0);
}
