// IECanvas.cpp : Defines the entry point for the DLL application.
//

#include "stdafx.h"
#include "IECanvas.h"
#include "IE4Events.h"
#include "Registry.h"
#include "objbase.h"

#include "jawt_md.h"
#include "eh.h"

#include "nothome_mswindows_IECanvas.h"

const char* CLSID_MozillaBrowserString = "{1339B54C-3453-11D2-93B9-000000000000}";

/*BOOL APIENTRY DllMain(HANDLE hModule, 
                       DWORD  ul_reason_for_call, 
                       LPVOID lpReserved
					 )
{

    return TRUE;
}*/

static void my_translator(unsigned code, EXCEPTION_POINTERS *)
{
   throw code;
}


// #define MESSAGE_IMPL 1

#define MSG_NAVIGATE2	(WM_USER+1001+1979)
#define MSG_RESIZE		(WM_USER+1002+1979)
#define MSG_REFRESH		(WM_USER+1003+1979)
#define MSG_GOHOME		(WM_USER+1004+1979)
#define MSG_GOFORWARD	(WM_USER+1005+1979)
#define MSG_GOBACK		(WM_USER+1006+1979)
#define MSG_SETFOCUS 	(WM_USER+1007+1979)
#define MSG_EXECSCRIPT 	(WM_USER+1008+1979)

CIECanvas::CIECanvas(JavaVM *pJavaVM, jobject pObj): 
	m_pJavaVM(pJavaVM), m_pObj(pObj), m_dwCookie(0) {

    AtlAxWinInit();
	m_pIE4Events = new CIE4Events(this, pJavaVM, pObj);
}

CIECanvas::~CIECanvas() {

}

BOOL CIECanvas::Initialize(HWND hwnd, int tryMozilla) {

	hwndParent = hwnd;
	
	/*if (tryMozilla) { // Initialize using mozilla..

		hwndChild = ::CreateWindow("AtlAxWin", CLSID_MozillaBrowserString, 
			WS_CHILD|WS_VISIBLE|WS_TABSTOP, 0,0,0,0, hwnd, NULL,
			::GetModuleHandle(NULL), NULL);

		// We should try to fallback to IE if mozilla not available...

	} else { // Initialize with IE5
*/
		hwndChild = ::CreateWindow("AtlAxWin", "Shell.Explorer.1", 
			WS_CHILD|WS_VISIBLE|WS_TABSTOP, 0,0,0,0, hwnd, NULL,
			::GetModuleHandle(NULL), NULL);
/*	}*/

    IUnknown *pUnk = NULL;
    AtlAxGetControl(hwndChild, &pUnk);
    
    pUnk->QueryInterface(IID_IWebBrowser2, (void**)&m_spBrowser);

    if (m_spBrowser) {
		m_spBrowser->put_Silent(VARIANT_TRUE);
        m_spBrowser->put_Visible(VARIANT_TRUE);

		// Set up the event sink
        BOOL bAdvised = AfxConnectionAdvise(pUnk /*m_spBrowser*/, DIID_DWebBrowserEvents2,
                                            m_pIE4Events->GetInterface(&IID_IUnknown),
                                            TRUE, &m_dwCookie);

        //printf("Advise for DWebBrowserEvents2 %s\n", bAdvised ? "succeeded" : "failed");
		
		m_spBrowser->put_Silent(VARIANT_TRUE);
		m_spBrowser->Refresh();
        CComVariant ve;
        CComVariant vurl("about:blank");
		m_spBrowser->Navigate2(&vurl, &ve, &ve, &ve, &ve);
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

        m_spBrowser->put_Visible(VARIANT_TRUE);
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

void CIECanvas::SetInnerHTML(const char *id, const char *html) {
	if (m_spBrowser) {
      try {
         HRESULT hr = CoInitialize(NULL);
         if (FAILED(hr)) {
            printf("CoInitialize Failed: %ld\n",hr);
            return;
         }
		   SHDocVw::IWebBrowser2Ptr browser = m_spBrowser;
		   MSHTML::IHTMLDocument3Ptr document = browser->GetDocument();
		   if (document == NULL)
			   return;

		   MSHTML::IHTMLElementPtr element = document->getElementById(_bstr_t(id));
		   if (element == NULL)
			   return;

		   element->innerHTML = _bstr_t(html);
      } catch (_com_error e) {
         printf("Exception in setting innerHTML %ld, %s\n", e.Error(), e.ErrorMessage());
      } catch (unsigned exception) {
         printf("Exception in setting innerHTML %ld, \n", exception);
      }
    }
}

char* CIECanvas::GetInnerHTML(const char *id) {
	if (m_spBrowser) {
      try {
         HRESULT hr = CoInitialize(NULL);
         if (FAILED(hr)) {
            printf("CoInitialize Failed: %ld\n",hr);
            return strdup("");
         }
		   SHDocVw::IWebBrowser2Ptr browser = m_spBrowser;
		   MSHTML::IHTMLDocument3Ptr document = browser->Document;
		   if (document == NULL)
			   return strdup("");

		   MSHTML::IHTMLElementPtr element = document->getElementById(_bstr_t(id));
		   if (element == NULL)
			   return strdup("");
		   
		   _bstr_t html = element->innerHTML;
		   return strdup(html);
      } catch (_com_error e) {
         printf("Exception in getting innerHTML %ld, %s\n", e.Error(), e.ErrorMessage());
      }
	}
	return strdup("");
}

void CIECanvas::SetClassName(const char *id, const char *className) {
	if (m_spBrowser) {
      try {
         HRESULT hr = CoInitialize(NULL);
         if (FAILED(hr)) {
            printf("CoInitialize Failed: %ld\n",hr);
            return;
         }
		   SHDocVw::IWebBrowser2Ptr browser = m_spBrowser;
		   MSHTML::IHTMLDocument3Ptr document = browser->GetDocument();
		   if (document == NULL)
			   return;

		   MSHTML::IHTMLElementPtr element = document->getElementById(_bstr_t(id));
		   if (element == NULL)
			   return;

		   element->className = _bstr_t(className);
      } catch (_com_error e) {
         printf("Exception in setting className %ld, %s\n", e.Error(), e.ErrorMessage());
      } catch (unsigned exception) {
         printf("Exception in setting className %ld, \n", exception);
      }
    }
}

char* CIECanvas::GetClassName(const char *id) {
	if (m_spBrowser) {
      try {
         HRESULT hr = CoInitialize(NULL);
         if (FAILED(hr)) {
            printf("CoInitialize Failed: %ld\n",hr);
            return strdup("");
         }
		   SHDocVw::IWebBrowser2Ptr browser = m_spBrowser;
		   MSHTML::IHTMLDocument3Ptr document = browser->Document;
		   if (document == NULL)
			   return strdup("");

		   MSHTML::IHTMLElementPtr element = document->getElementById(_bstr_t(id));
		   if (element == NULL)
			   return strdup("");
		   
		   _bstr_t className = element->className;
		   return strdup(className);
      } catch (_com_error e) {
         printf("Exception in getting className %ld, %s\n", e.Error(), e.ErrorMessage());
      }
	}
	return strdup("");
}

void CIECanvas::ExecuteJavaScript(const char *javaScript) {
	if (m_spBrowser) {
      try {
         HRESULT hr = CoInitialize(NULL);
         if (FAILED(hr)) {
            printf("CoInitialize Failed: %ld\n",hr);
            return;
         }

	      SHDocVw::IWebBrowser2Ptr browser = m_spBrowser;
	      MSHTML::IHTMLDocument2Ptr document = browser->Document;
         if (document == NULL)
            return;

         MSHTML::IHTMLWindow2Ptr window = document->parentWindow;
         if (window == NULL)
            return;

         window->execScript(javaScript, "javascript");
      } catch (_com_error e) {
         printf("Exception in running Javascript %ld, %s\n", e.Error(), e.ErrorMessage());
      }
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
   _bstr_t script;
} ThreadParam;

// forward..
static void WINAPIV runATL(LPVOID);

JNIEXPORT void JNICALL Java_nothome_mswindows_IECanvas_initialize(JNIEnv *pEnv, jobject pCanvas, jboolean bUseMozilla) {

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
		::AfxMessageBox("Needs AWT version 1.3 or newer!");
		printf("Needs AWT version 1.3 or newer!\n"); return; }
	ds = awt.GetDrawingSurface(pEnv, pCanvas);
	if (ds == NULL) { 
		::AfxMessageBox("unable to get drawing surface!");
		printf("unable to get drawing surface!\n"); return; }
	lock = ds->Lock(ds);
	if ((lock & JAWT_LOCK_ERROR) != 0) { 
		::AfxMessageBox("unable to lock drawing surface!");
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
		::AfxMessageBox("Unable to get HWND to parent Canvas, aborting!");
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
		::AfxMessageBox("error when doing global ref to obj..");
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

		// old message based method
		/*if (msg.message == MSG_NAVIGATE2) {
			ThreadParam *pParam = (ThreadParam*) msg.lParam;
			pIECanvas->Navigate2(pParam->szURL);
			delete pThreadParam;
			
		} else if (msg.message == MSG_REFRESH) {
			pIECanvas->Refresh();
		} else */

		if (msg.message == MSG_RESIZE) {
		    RECT rc;
			::GetWindowRect(pIECanvas->GetHWNDParent(), &rc);
			::SetWindowPos(pIECanvas->GetHWNDChild(), NULL, 0, 0, 
				rc.right-rc.left, rc.bottom-rc.top, 
				SWP_NOZORDER|SWP_NOACTIVATE|SWP_SHOWWINDOW|SWP_NOMOVE);

		} else if (msg.message == MSG_GOHOME) {
			pIECanvas->GoHome();
		} else if (msg.message == MSG_GOFORWARD) {
			pIECanvas->GoForward();
		} else if (msg.message == MSG_GOBACK) {
			pIECanvas->GoBack();
		} else if (msg.message == MSG_SETFOCUS) {
			printf("focus hack!\n");
			SetFocus(GetParent(GetParent(pIECanvas->GetHWNDChild())));
		} else if (msg.message == MSG_EXECSCRIPT) {
			ThreadParam *pParam = (ThreadParam*) msg.lParam;
			if(pParam){
				pIECanvas->ExecuteJavaScript(pParam->script);
			}
			delete(pParam);
      }
		DispatchMessage(&msg);
    }
}


JNIEXPORT void JNICALL Java_nothome_mswindows_IECanvas_sendurl(JNIEnv *pEnv, jobject pObj, jstring string) {
	CIECanvas* pIECanvas = NULL;

    const char *str	= pEnv->GetStringUTFChars(string, 0);
	ThreadParam *pThreadParam = new ThreadParam;
    strcpy(pThreadParam->szURL,str);
    pEnv->ReleaseStringUTFChars(string, str);

	jint hashCode = Registry::GetHashCode(pEnv, pObj);
	Registry::Lookup((void*)hashCode, (void*&)pIECanvas);

#ifdef MESSAGE_IMPL
	// old message based method
	if (pIECanvas != NULL)
		PostMessage(pIECanvas->GetHWNDChild(), MSG_NAVIGATE2, 0, (long)pThreadParam);
#else
	if (pIECanvas != NULL)
		pIECanvas->Navigate2(pThreadParam->szURL);
#endif
		
}


JNIEXPORT void JNICALL Java_nothome_mswindows_IECanvas_resizeControl(JNIEnv *pEnv, jobject pObj) {
	CIECanvas* pIECanvas = NULL;

	jint hashCode = Registry::GetHashCode(pEnv, pObj);
	Registry::Lookup((void*)hashCode, (void*&)pIECanvas);

#ifdef MESSAGE_IMPL
	if (pIECanvas != NULL)
		PostMessage(pIECanvas->GetHWNDChild(), MSG_RESIZE, 0, 0);
#else
	if (pIECanvas != NULL) {
		RECT rc;
		::GetWindowRect(pIECanvas->GetHWNDParent(), &rc);
		::SetWindowPos(pIECanvas->GetHWNDChild(), NULL, 0, 0,
			rc.right-rc.left, rc.bottom-rc.top,
			SWP_NOZORDER|SWP_NOACTIVATE|SWP_SHOWWINDOW|SWP_NOMOVE);
	}
#endif

}

JNIEXPORT void JNICALL Java_nothome_mswindows_IECanvas_gohome(JNIEnv *pEnv, jobject pObj) {
	CIECanvas* pIECanvas = NULL;
	jint hashCode = Registry::GetHashCode(pEnv, pObj);
	Registry::Lookup((void*)hashCode, (void*&)pIECanvas);
	if (pIECanvas != NULL)
#ifdef MESSAGE_IMPL
		PostMessage(pIECanvas->GetHWNDChild(), MSG_GOHOME, 0, 0);
#else
		pIECanvas->GoHome();
#endif
}

JNIEXPORT void JNICALL Java_nothome_mswindows_IECanvas_rld(JNIEnv *pEnv, jobject pObj) {
	CIECanvas* pIECanvas = NULL;
	jint hashCode = Registry::GetHashCode(pEnv, pObj);
	Registry::Lookup((void*)hashCode, (void*&)pIECanvas);

#ifdef MESSAGE_IMPL
	// old message based method
	if (pIECanvas != NULL)
		PostMessage(pIECanvas->GetHWNDChild(), MSG_REFRESH, 0, 0);
#else
	if (pIECanvas != NULL)
		pIECanvas->Refresh();
#endif

}

JNIEXPORT void JNICALL Java_nothome_mswindows_IECanvas_gofwd(JNIEnv *pEnv, jobject pObj) {
	CIECanvas* pIECanvas = NULL;
	jint hashCode = Registry::GetHashCode(pEnv, pObj);
	Registry::Lookup((void*)hashCode, (void*&)pIECanvas);

	if (pIECanvas != NULL)
#ifdef MESSAGE_IMPL
		PostMessage(pIECanvas->GetHWNDChild(), MSG_GOFORWARD, 0, 0);
#else
		pIECanvas->GoForward();
#endif
}


JNIEXPORT void JNICALL Java_nothome_mswindows_IECanvas_gobackwd(JNIEnv *pEnv, jobject pObj) {
	CIECanvas* pIECanvas = NULL;
	jint hashCode = Registry::GetHashCode(pEnv, pObj);
	Registry::Lookup((void*)hashCode, (void*&)pIECanvas);

	if (pIECanvas != NULL)
#ifdef MESSAGE_IMPL
		PostMessage(pIECanvas->GetHWNDChild(), MSG_GOBACK, 0, 0);
#else
		pIECanvas->GoBack();
#endif
}


JNIEXPORT void JNICALL Java_nothome_mswindows_IECanvas_setfocus(JNIEnv *pEnv, jobject pObj) {
	CIECanvas* pIECanvas = NULL;
	jint hashCode = Registry::GetHashCode(pEnv, pObj);
	Registry::Lookup((void*)hashCode, (void*&)pIECanvas);

#ifdef MESSAGE_IMPL
	if (pIECanvas != NULL)
		PostMessage(pIECanvas->GetHWNDChild(), MSG_SETFOCUS, 0, 0);
#endif
}

JNIEXPORT void JNICALL Java_nothome_mswindows_IECanvas_setInnerHtml
  (JNIEnv *pEnv, jobject pObj, jstring id, jstring html)
{
	CIECanvas* pIECanvas = NULL;
	jint hashCode = Registry::GetHashCode(pEnv, pObj);
	Registry::Lookup((void*)hashCode, (void*&)pIECanvas);

	const char *idStr	= pEnv->GetStringUTFChars(id, 0);
	const char *htmlStr	= pEnv->GetStringUTFChars(html, 0);

	if (pIECanvas != NULL)
#ifdef MESSAGE_IMPL
#else
		pIECanvas->SetInnerHTML(idStr, htmlStr);
#endif

	pEnv->ReleaseStringUTFChars(id, idStr);
	pEnv->ReleaseStringUTFChars(html, htmlStr);

}

JNIEXPORT jstring JNICALL Java_nothome_mswindows_IECanvas_getInnerHtml
  (JNIEnv *pEnv, jobject pObj, jstring id)
{
	CIECanvas* pIECanvas = NULL;
	jint hashCode = Registry::GetHashCode(pEnv, pObj);
	Registry::Lookup((void*)hashCode, (void*&)pIECanvas);

	const char *idStr	= pEnv->GetStringUTFChars(id, 0);
	char *htmlStr = strdup("");

	if (pIECanvas != NULL)
#ifdef MESSAGE_IMPL
#else
		htmlStr = pIECanvas->GetInnerHTML(idStr);
#endif

    jstring html = pEnv->NewStringUTF(htmlStr);
	free(htmlStr);
	pEnv->ReleaseStringUTFChars(id, idStr);
	return html;
}

JNIEXPORT void JNICALL Java_nothome_mswindows_IECanvas_setClassName
  (JNIEnv *pEnv, jobject pObj, jstring id, jstring className)
{
	CIECanvas* pIECanvas = NULL;
	jint hashCode = Registry::GetHashCode(pEnv, pObj);
	Registry::Lookup((void*)hashCode, (void*&)pIECanvas);

	const char *idStr	= pEnv->GetStringUTFChars(id, 0);
	const char *classNameStr = pEnv->GetStringUTFChars(className, 0);

	if (pIECanvas != NULL)
#ifdef MESSAGE_IMPL
#else
	pIECanvas->SetClassName(idStr, classNameStr);
#endif

	pEnv->ReleaseStringUTFChars(id, idStr);
	pEnv->ReleaseStringUTFChars(className, classNameStr);
}

JNIEXPORT jstring JNICALL Java_nothome_mswindows_IECanvas_getClassName
  (JNIEnv *pEnv, jobject pObj, jstring id)
{
	CIECanvas* pIECanvas = NULL;
	jint hashCode = Registry::GetHashCode(pEnv, pObj);
	Registry::Lookup((void*)hashCode, (void*&)pIECanvas);

	const char *idStr	= pEnv->GetStringUTFChars(id, 0);
	char *classNameStr = strdup("");

	if (pIECanvas != NULL)
#ifdef MESSAGE_IMPL
#else
		classNameStr = pIECanvas->GetClassName(idStr);
#endif

	jstring className = pEnv->NewStringUTF(classNameStr);
	free(classNameStr);
	pEnv->ReleaseStringUTFChars(id, idStr);
	return className;
}


JNIEXPORT void JNICALL Java_nothome_mswindows_IECanvas_executeJavascript
  (JNIEnv *pEnv, jobject pObj, jstring javaScript)
{
	CIECanvas* pIECanvas = NULL;

	jint hashCode = Registry::GetHashCode(pEnv, pObj);
	Registry::Lookup((void*)hashCode, (void*&)pIECanvas);

	const jchar *javaScriptStr	= pEnv->GetStringChars(javaScript, 0);
	int length = pEnv->GetStringLength(javaScript); 
	char* rtn = (char*)malloc( length*2+1 );

	int size = ::WideCharToMultiByte( CP_ACP, 0, (LPCWSTR)javaScriptStr, length, rtn,(length*2+1),NULL,NULL);
	rtn[size] = 0;


    ThreadParam *pThreadParam = new ThreadParam;
    pThreadParam->script = rtn;
	delete rtn;

   // Only PostMEssage seems to work for executing javascript

	if (pIECanvas != NULL)
//#ifdef MESSAGE_IMPL
		PostMessage(pIECanvas->GetHWNDChild(), MSG_EXECSCRIPT, 0, (long)pThreadParam);
//#else
//		pIECanvas->ExecuteJavaScript(javaScriptStr);
//#endif
	pEnv->ReleaseStringChars(javaScript, javaScriptStr);
}

BOOL APIENTRY DllMain(HANDLE hModule, 
                      DWORD  ul_reason_for_call, 
                      LPVOID lpReserved)
{
    switch( ul_reason_for_call ) {
    case DLL_PROCESS_ATTACH:
       _set_se_translator(my_translator);
       break;
    case DLL_THREAD_ATTACH:
       break;
    case DLL_THREAD_DETACH:
       break;
    case DLL_PROCESS_DETACH:
       break;
    }
    return TRUE;
}