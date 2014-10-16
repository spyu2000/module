// dogdemo.h : main header file for the DOGDEMO application
//

#if !defined(AFX_DOGDEMO_H__3FA0D833_39E7_11D2_9F0B_00C00C1026E0__INCLUDED_)
#define AFX_DOGDEMO_H__3FA0D833_39E7_11D2_9F0B_00C00C1026E0__INCLUDED_

#if _MSC_VER >= 1000
#pragma once
#endif // _MSC_VER >= 1000

#ifndef __AFXWIN_H__
	#error include 'stdafx.h' before including this file for PCH
#endif

#include "resource.h"		// main symbols

/////////////////////////////////////////////////////////////////////////////
// CDogdemoApp:
// See dogdemo.cpp for the implementation of this class
//

class CDogdemoApp : public CWinApp
{
public:
	CDogdemoApp();

// Overrides
	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CDogdemoApp)
	public:
	virtual BOOL InitInstance();
	//}}AFX_VIRTUAL

// Implementation

	//{{AFX_MSG(CDogdemoApp)
		// NOTE - the ClassWizard will add and remove member functions here.
		//    DO NOT EDIT what you see in these blocks of generated code !
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()
};


/////////////////////////////////////////////////////////////////////////////

//{{AFX_INSERT_LOCATION}}
// Microsoft Developer Studio will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_DOGDEMO_H__3FA0D833_39E7_11D2_9F0B_00C00C1026E0__INCLUDED_)
