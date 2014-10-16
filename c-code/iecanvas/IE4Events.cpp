// IE4Events.cpp : implementation file
//
// This source code is provided as-is, without warranty.
//
// Created by Scott Roberts
// Microsoft Developer Support - Internet Client SDK
/////////////////////////////////////////////////////////

#include "stdafx.h"
#include "IECanvas.h"
#include "IE4Events.h"
#include "CallBackDispatch.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

/////////////////////////////////////////////////////////////////////////////
// CIE4Events

IMPLEMENT_DYNCREATE(CIE4Events, CCmdTarget)

CIE4Events::CIE4Events(CIECanvas* pParent, JavaVM *pJavaVM, jobject pObj): 
	m_pParent(pParent), m_cRef(0), m_pJavaVM(pJavaVM), m_pObj(pObj) {

	ASSERT(m_pParent);

	EnableAutomation();  // Needed in order to sink events.
}

CIE4Events::~CIE4Events() { }

STDMETHODIMP CIE4Events::QueryInterface(REFIID iid, void **ppv) { 

	*ppv = NULL;
	if (iid == IID_IUnknown || iid == IID_IDocHostUIHandler) {
		*ppv = static_cast<IDocHostUIHandler *>(this);
	} 
	if (*ppv) {
		AddRef();
		return S_OK;
	} else 
		return E_NOINTERFACE;
}

STDMETHODIMP_(ULONG) CIE4Events::AddRef() { 
	return ++m_cRef; // NOT thread-safe
} 

STDMETHODIMP_(ULONG) CIE4Events::Release() { 
	printf("releasing with count : %d\n", m_cRef);
	--m_cRef; // NOT thread-safe
	if (m_cRef == 0) {
		delete this;
		return 0;
	} else
		return m_cRef;
	
}


BEGIN_MESSAGE_MAP(CIE4Events, CCmdTarget)
	//{{AFX_MSG_MAP(CIE4Events)
		// NOTE - the ClassWizard will add and remove mapping macros here.
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

BEGIN_DISPATCH_MAP(CIE4Events, CCmdTarget)
	DISP_FUNCTION_ID(CIE4Events, "BeforeNavigate2", DISPID_BEFORENAVIGATE2,
                    OnBeforeNavigate2, VT_EMPTY, VTS_DISPATCH VTS_PVARIANT
                    VTS_PVARIANT VTS_PVARIANT VTS_PVARIANT VTS_PVARIANT VTS_PBOOL)

	DISP_FUNCTION_ID(CIE4Events, "CommandStateChange", DISPID_COMMANDSTATECHANGE,
                    OnCommandStateChange, VT_EMPTY, VTS_I4 VTS_BOOL)

	DISP_FUNCTION_ID(CIE4Events, "DocumentComplete", DISPID_DOCUMENTCOMPLETE,
                    OnDocumentComplete, VT_EMPTY, VTS_DISPATCH VTS_PVARIANT)

	DISP_FUNCTION_ID(CIE4Events, "DownloadComplete", DISPID_DOWNLOADCOMPLETE,
                    OnDownloadComplete, VT_EMPTY, VTS_NONE)

	DISP_FUNCTION_ID(CIE4Events, "NavigateComplete2", DISPID_NAVIGATECOMPLETE2,
                    OnNavigateComplete2, VT_EMPTY, VTS_DISPATCH VTS_PVARIANT)

	DISP_FUNCTION_ID(CIE4Events, "NewWindow2", DISPID_NEWWINDOW2, 
                    OnNewWindow2, VT_EMPTY, VTS_DISPATCH VTS_PBOOL)

	DISP_FUNCTION_ID(CIE4Events, "OnQuit", DISPID_ONQUIT, OnQuit, VT_EMPTY, VTS_NONE)

	DISP_FUNCTION_ID(CIE4Events, "ProgressChange", DISPID_PROGRESSCHANGE,
                    OnProgressChange, VT_EMPTY, VTS_I4 VTS_I4)

	DISP_FUNCTION_ID(CIE4Events, "StatusTextChange", DISPID_STATUSTEXTCHANGE,
                    OnStatusTextChange, VT_EMPTY, VTS_BSTR)

	DISP_FUNCTION_ID(CIE4Events, "TitleChange", DISPID_TITLECHANGE,
                    OnTitleChange, VT_EMPTY, VTS_BSTR)
END_DISPATCH_MAP()

//
// Add DIID_DWebBrowserEvents2 to the interface map to tell
// IE4 that we support this event sink.  IE4 will QI for 
// this interface when we call AfxConnectionAdvise.  If IE4
// does not find this interface, it will then QI for IDispatch.
//
BEGIN_INTERFACE_MAP(CIE4Events, CCmdTarget)
	INTERFACE_PART(CIE4Events, DIID_DWebBrowserEvents2, Dispatch)
END_INTERFACE_MAP()


/////////////////////////////////////////////////////////////////////////////
// CIE4Events event handlers
void CIE4Events::OnBeforeNavigate2(LPDISPATCH pDisp, VARIANT* URL, VARIANT* Flags, VARIANT* TargetFrameName, VARIANT* PostData, VARIANT* Headers, BOOL* Cancel) {
	USES_CONVERSION;
	CString strEvt(OLE2T(URL->bstrVal));

	// Attach this thread.
	JNIEnv *pEnv	= NULL;
	m_pJavaVM->AttachCurrentThread((void **)&pEnv,NULL);
	
	// Inform the java object that the child has been created.
	if (pEnv && m_pObj) {
		jclass clazz = pEnv->GetObjectClass(m_pObj);
		if (clazz) {
			jmethodID mid = pEnv->GetMethodID(clazz, "onBeforeNavigate2", "(Ljava/lang/String;)V");
			if (mid == 0)
				return;
			//pEnv->CallVoidMethod(m_pObj, mid);
			//pEnv->CallVoidMethod(frame, mid, (jshort)fwKeys, (jshort)zDelta, (jlong)xPos, (jlong)yPos);
			pEnv->CallVoidMethod(m_pObj, mid, pEnv->NewStringUTF(strEvt));
		}
	}
	// Detach this thread.
	m_pJavaVM->DetachCurrentThread();
}

HRESULT CIE4Events::ShowContextMenu(DWORD dwID, POINT FAR* ppt, IUnknown FAR* pcmdtReserved, IDispatch FAR* pdispReserved) {
	m_pParent->AddEventToList("ShowContextMenu");

	// Attach this thread.
	JNIEnv *pEnv	= NULL;
	m_pJavaVM->AttachCurrentThread((void **)&pEnv,NULL);

	boolean showIEmenu = S_FALSE;

	if (pEnv && m_pObj) {
		jclass clazz = pEnv->GetObjectClass(m_pObj);
		if (clazz) {
			//jmethodID mid = pEnv->GetMethodID(clazz, "showContextMenu", "()V");
			jmethodID mid = pEnv->GetMethodID(clazz, "showContextMenu", "(II)Z");
			if (mid) {
				if (pEnv->CallBooleanMethod(m_pObj, mid, (jint) ppt->x, (jint) ppt->y) == JNI_TRUE) {
					showIEmenu = S_OK;
				} else {
					showIEmenu = S_FALSE;
				}
				//pEnv->CallVoidMethod(m_pObj, mid);
			}

		}
	}
	m_pJavaVM->DetachCurrentThread();

	if (showIEmenu == S_OK) { // Show Context Menu
		return S_FALSE;
	}

	return S_OK;
}

// From the MS documentation on the OnCommandStateChange 

// CSC_UPDATECOMMANDS -1 The enabled state of a toolbar button might have changed; the Enable parameter should be ignored. 
// CSC_NAVIGATEFORWARD 1 The enabled state of the Forward button has changed. 
// CSC_NAVIGATEBACK 2 The enabled state of the Back button has changed. 

void CIE4Events::OnCommandStateChange(long lCommand, BOOL bEnable) {
//	m_pParent->AddEventToList("OnCommandStateChange");

	// Attach this thread.
	JNIEnv *pEnv	= NULL;
	m_pJavaVM->AttachCurrentThread((void **)&pEnv,NULL);

	if (pEnv && m_pObj) {
		jclass clazz = pEnv->GetObjectClass(m_pObj);
		if (clazz) {
			jmethodID mid = pEnv->GetMethodID(clazz, "onCommandStateChange", "(IZ)V");
			if (mid) {
				//jlong command = (jlong) lCommand;
				//jboolean enable = bEnable ? JNI_TRUE : JNI_FALSE; //(jboolean) bEnable + 2;
				//printf("callback values: %d, %d\n", lCommand, bEnable);
				pEnv->CallVoidMethod(m_pObj, mid, (jint) lCommand, (jboolean) (bEnable ? JNI_TRUE : JNI_FALSE));
			} //else
				//printf("no callback!  values: %d, %d\n", lCommand, bEnable);

		}
	}
	m_pJavaVM->DetachCurrentThread();
//	m_pParent->OnCommandStateChange(lCommand, bEnable);
}

void CIE4Events::OnDocumentComplete(LPDISPATCH pDisp, VARIANT* URL) {
	USES_CONVERSION;
//	m_pParent->AddEventToList("OnDocumentComplete");

	// Attach this thread.
	JNIEnv *pEnv	= NULL;
	m_pJavaVM->AttachCurrentThread((void **)&pEnv,NULL);

	CString strEvt(OLE2T(URL->bstrVal));

	m_pParent->UpdateUIHandler();

	if (pEnv && m_pObj) {
		jclass clazz = pEnv->GetObjectClass(m_pObj);
		if (clazz) {
			jmethodID mid = pEnv->GetMethodID(clazz, "onDocumentComplete", "(Ljava/lang/String;)V");
			if (mid)
				pEnv->CallVoidMethod(m_pObj, mid, pEnv->NewStringUTF(strEvt));
		}
	}
	m_pJavaVM->DetachCurrentThread();
}

void CIE4Events::OnDownloadComplete() {
//	m_pParent->AddEventToList("OnDownloadComplete");
	
	// Attach this thread.
	JNIEnv *pEnv	= NULL;
	m_pJavaVM->AttachCurrentThread((void **)&pEnv,NULL);

	if (pEnv && m_pObj) {
		jclass clazz = pEnv->GetObjectClass(m_pObj);
		if (clazz) {
			jmethodID mid = pEnv->GetMethodID(clazz, "onDownloadComplete", "()V");
			if (mid)
				pEnv->CallVoidMethod(m_pObj, mid);
		}
	}
	m_pJavaVM->DetachCurrentThread();
}

void CIE4Events::OnNavigateComplete2(LPDISPATCH pDisp, VARIANT* URL) {
	USES_CONVERSION;
//	m_pParent->AddEventToList("OnNavigateComplete2");

	// Attach this thread.
	JNIEnv *pEnv	= NULL;
	m_pJavaVM->AttachCurrentThread((void **)&pEnv,NULL);

	CString strEvt(OLE2T(URL->bstrVal));

	m_pParent->AddEventToList(strEvt);
	if (pEnv && m_pObj) {
		jclass clazz = pEnv->GetObjectClass(m_pObj);
		if (clazz) {
			jmethodID mid = pEnv->GetMethodID(clazz, "onNavigateComplete2", "(Ljava/lang/String;)V");
			if (mid)
				pEnv->CallVoidMethod(m_pObj, mid, pEnv->NewStringUTF(strEvt));
		}
	}
	m_pJavaVM->DetachCurrentThread();
}

void CIE4Events::OnNewWindow2(LPDISPATCH* ppDisp, BOOL* Cancel) {
	m_pParent->AddEventToList("OnNewWindow2");

	// How do we handle additional windows?

	// 1. create new IECanvas java object,
	// 2. create accompanying IECanvas C++ object
	// 3. set the ppDisp pointer to the new IECanvas C++ object.

	// Yet to be implemented... -Torgeir

	// Attach this thread.
/*	JNIEnv *pEnv	= NULL;
	m_pJavaVM->AttachCurrentThread((void **)&pEnv,NULL);

	if (pEnv && m_pObj) {
		jclass clazz = pEnv->GetObjectClass(m_pObj);
		if (clazz) {
			jmethodID mid = pEnv->GetMethodID(clazz, "onNewWindow2", "(Ljava/lang/String;)V");
			if (mid)
				pEnv->CallVoidMethod(m_pObj, mid, pEnv->NewStringUTF(strEvt));
		}
	}
	m_pJavaVM->DetachCurrentThread();*/
}

void CIE4Events::OnProgressChange(long lProgress, long lProgressMax) {
//	m_pParent->AddEventToList("OnProgressChange");

	// Attach this thread.
	JNIEnv *pEnv	= NULL;
	m_pJavaVM->AttachCurrentThread((void **)&pEnv,NULL);

	printf("jni: progress: %d, %d\n", lProgress, lProgressMax);
	if (pEnv && m_pObj) {
		jclass clazz = pEnv->GetObjectClass(m_pObj);
		if (clazz) {
			jmethodID mid = pEnv->GetMethodID(clazz, "onProgressChange", "(II)V");
			if (mid)
				pEnv->CallVoidMethod(m_pObj, mid, (jint) lProgress, (jint) lProgressMax);
		}
	} 
	m_pJavaVM->DetachCurrentThread();
}

void CIE4Events::OnStatusTextChange(LPCTSTR lpszText) {
//	m_pParent->AddEventToList("OnStatusTextChange");

	// Attach this thread.
	JNIEnv *pEnv	= NULL;
	m_pJavaVM->AttachCurrentThread((void **)&pEnv,NULL);

	if (pEnv && m_pObj) {
		jclass clazz = pEnv->GetObjectClass(m_pObj);
		if (clazz) {
			jmethodID mid = pEnv->GetMethodID(clazz, "onStatusTextChange", "(Ljava/lang/String;)V");
			if (mid)
				pEnv->CallVoidMethod(m_pObj, mid, pEnv->NewStringUTF(lpszText));
		}
	} 
	m_pJavaVM->DetachCurrentThread();
}

void CIE4Events::OnTitleChange(LPCTSTR lpszText) {
//	m_pParent->AddEventToList("OnTitleChange");
	// Attach this thread.
	JNIEnv *pEnv	= NULL;
	m_pJavaVM->AttachCurrentThread((void **)&pEnv,NULL);

	if (pEnv && m_pObj) {
		jclass clazz = pEnv->GetObjectClass(m_pObj);
		if (clazz) {
			jmethodID mid = pEnv->GetMethodID(clazz, "onTitleChange", "(Ljava/lang/String;)V");
			if (mid)
				pEnv->CallVoidMethod(m_pObj, mid, pEnv->NewStringUTF(lpszText));
		}
	} 
	m_pJavaVM->DetachCurrentThread();
}

void CIE4Events::OnQuit() {
//	m_pParent->AddEventToList("OnQuit");

	// Attach this thread.
	JNIEnv *pEnv	= NULL;
	m_pJavaVM->AttachCurrentThread((void **)&pEnv,NULL);

	if (pEnv && m_pObj) {
		jclass clazz = pEnv->GetObjectClass(m_pObj);
		if (clazz) {
			jmethodID mid = pEnv->GetMethodID(clazz, "onQuit", "()V");
			if (mid)
				pEnv->CallVoidMethod(m_pObj, mid);
		}
	} 
	m_pJavaVM->DetachCurrentThread();
}


HRESULT CIE4Events::GetExternal(IDispatch** ppDispatch) {

   m_pParent->AddEventToList("GetExternal");

   // return the IDispatch we have for extending the Dynamic HTML Object Model   
   CCallBackDispatch* pDispatch = new CCallBackDispatch(m_pJavaVM, m_pObj);   
	pDispatch->AddRef();

	*ppDispatch = (IDispatch*)pDispatch;

   return S_OK;
}
