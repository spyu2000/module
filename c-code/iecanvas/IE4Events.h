

#if !defined(AFX_IE4EVENTS_H__)
#define AFX_IE4EVENTS_H__

#if _MSC_VER >= 1000
#pragma once
#endif // _MSC_VER >= 1000


class CIE4Events: public CCmdTarget, public IDocHostUIHandler {

	DECLARE_DYNCREATE(CIE4Events)

public:
	CIE4Events(CIECanvas* pParent = NULL, JavaVM *pJavaVM = NULL, jobject pObj = NULL);
	virtual ~CIE4Events();

	STDMETHODIMP QueryInterface(REFIID riid, void **ppvObject);
	STDMETHODIMP_(ULONG) AddRef();
	STDMETHODIMP_(ULONG) Release();

protected:
	CIECanvas	*m_pParent;
	ULONG		m_cRef;
	JavaVM		*m_pJavaVM;
	jobject		m_pObj;


	// IE4 Events
	void OnBeforeNavigate2(LPDISPATCH pDisp, VARIANT* URL, VARIANT* Flags,
                          VARIANT* TargetFrameName, VARIANT* PostData,
                          VARIANT* Headers, BOOL* Cancel);
	void OnCommandStateChange(long lCommand, BOOL bEnable);
	void OnDocumentComplete(LPDISPATCH pDisp, VARIANT* URL);
	void OnDownloadComplete();
	void OnNavigateComplete2(LPDISPATCH pDisp, VARIANT* URL);
	void OnNewWindow2(LPDISPATCH* ppDisp, BOOL* Cancel);
	void OnProgressChange(long lProgress, long lProgressMax);
	void OnStatusTextChange(LPCTSTR lpszText);
	void OnTitleChange(LPCTSTR lpszText);
	void OnQuit();


	STDMETHOD(ShowContextMenu)(DWORD dwID, POINT FAR* ppt, IUnknown FAR* pcmdtReserved,
		IDispatch FAR* pdispReserved);
        
	STDMETHOD(GetHostInfo)(DOCHOSTUIINFO FAR *pInfo) {
		m_pParent->AddEventToList("GetHostInfo");
		return S_OK;
	}
        
	STDMETHOD(ShowUI)(DWORD dwID, IOleInPlaceActiveObject FAR* pActiveObject,
                    IOleCommandTarget FAR* pCommandTarget,
                    IOleInPlaceFrame  FAR* pFrame,
                    IOleInPlaceUIWindow FAR* pDoc) {
		m_pParent->AddEventToList("ShowUI");
		return S_OK;
	}
        
	STDMETHOD(HideUI)(void) {
		m_pParent->AddEventToList("HideUI");
		return S_OK;
	}
        
	STDMETHOD(UpdateUI)(void) {
		m_pParent->AddEventToList("UpdateUI");
		return S_OK;
	}
        
	STDMETHOD(EnableModeless)(BOOL fEnable) {
		m_pParent->AddEventToList("EnableModeless");
		return S_OK;
	}
   
	STDMETHOD(OnDocWindowActivate)(BOOL fActivate) {
		m_pParent->AddEventToList("OnDocWindowActivate");
		return S_OK;
	}
   
	STDMETHOD(OnFrameWindowActivate)(BOOL fActivate) {
		m_pParent->AddEventToList("OnFrameWindowActivate");
		return S_OK;
	}
   
	STDMETHOD(ResizeBorder)(LPCRECT prcBorder, IOleInPlaceUIWindow FAR* pUIWindow,
                           BOOL fRameWindow) {
		m_pParent->AddEventToList("ResizeBorder");
		return S_OK;
	}
   
	STDMETHOD(TranslateAccelerator)(LPMSG lpMsg, const GUID FAR* pguidCmdGroup,
                                   DWORD nCmdID) {
 		m_pParent->AddEventToList("TranslateAccelerator");

		return S_OK;
	}
   
	STDMETHOD(GetOptionKeyPath)(LPOLESTR FAR* pchKey, DWORD dw) {
		m_pParent->AddEventToList("GetOptionKeyPath");
		return S_OK;
	}
   
	STDMETHOD(GetDropTarget)(IDropTarget* pDropTarget,
                            IDropTarget** ppDropTarget) {
		m_pParent->AddEventToList("GetDropTarget");
		return S_OK;
	}
   
	STDMETHOD(GetExternal)(IDispatch** ppDispatch);
   
	STDMETHOD(TranslateUrl)(DWORD dwTranslate, OLECHAR* pchURLIn,
                           OLECHAR** ppchURLOut) {
		m_pParent->AddEventToList("TranslateUrl");
		return S_OK;
	}
   
	STDMETHOD(FilterDataObject)(IDataObject* pDO, IDataObject** ppDORet) {
		m_pParent->AddEventToList("FilterDataObject");
		return S_OK;
	}


	DECLARE_MESSAGE_MAP()
	DECLARE_DISPATCH_MAP()
	DECLARE_INTERFACE_MAP()
};


#endif // !defined(AFX_IE4EVENTS_H__)
