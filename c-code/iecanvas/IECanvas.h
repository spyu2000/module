// IECanvas.h : include file for standard system include files,
//  or project specific include files that are used frequently, but
//      are changed infrequently
//

#if !defined(AFX_IECANVAS_H____INCLUDED_)
#define AFX_IECANVAS_H____INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "jni.h"

class CIE4Events;

class CIECanvas {

public:
	CIECanvas(JavaVM *pJavaVM, jobject pObj);
	~CIECanvas();

public:
	BOOL					Initialize(HWND hwnd, int tryMozilla);
	void					UnInitialize();

	void					Navigate2(char *pURL);
	void					Refresh();
	void					GoForward();
	void					GoBack();
	void					GoHome();
	void					SetInnerHTML(const char *id, const char *html);
	char*					GetInnerHTML(const char *id);
	void					SetClassName(const char *id, const char *className);
	char*					GetClassName(const char *id);
	void					ExecuteJavaScript(const char *javaScript);

	HWND					GetHWNDChild();
	HWND					GetHWNDParent();

	void					UpdateUIHandler();
	void					AddEventToList(CString strEvent);

protected:
	CIE4Events				*m_pIE4Events;
	DWORD					m_dwCookie;

	HWND					hwndChild, hwndParent;
	CComPtr<IWebBrowser2>	m_spBrowser;
	JavaVM					*m_pJavaVM;
	jobject					m_pObj;
};



// TODO: reference additional headers your program requires here

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_IECANVAS_H____INCLUDED_)
