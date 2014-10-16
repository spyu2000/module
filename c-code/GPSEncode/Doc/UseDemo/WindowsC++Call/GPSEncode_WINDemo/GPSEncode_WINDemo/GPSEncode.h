// GPSEncode.h : main header file for the GPSENCODE application
//

#if !defined(AFX_GPSENCODE_H__D70F8B58_3CBB_45A4_8330_348E771B8C77__INCLUDED_)
#define AFX_GPSENCODE_H__D70F8B58_3CBB_45A4_8330_348E771B8C77__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#ifndef __AFXWIN_H__
	#error include 'stdafx.h' before including this file for PCH
#endif

#include "resource.h"		// main symbols

/////////////////////////////////////////////////////////////////////////////
// CGPSEncodeApp:
// See GPSEncode.cpp for the implementation of this class
//

class CGPSEncodeApp : public CWinApp
{
public:
	CGPSEncodeApp();

// Overrides
	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CGPSEncodeApp)
	public:
	virtual BOOL InitInstance();
	//}}AFX_VIRTUAL

// Implementation

	//{{AFX_MSG(CGPSEncodeApp)
		// NOTE - the ClassWizard will add and remove member functions here.
		//    DO NOT EDIT what you see in these blocks of generated code !
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()
};


/////////////////////////////////////////////////////////////////////////////

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_GPSENCODE_H__D70F8B58_3CBB_45A4_8330_348E771B8C77__INCLUDED_)
