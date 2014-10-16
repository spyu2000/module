// stdafx.h : include file for standard system include files,
//  or project specific include files that are used frequently, but
//      are changed infrequently
//

#if !defined(AFX_STDAFX_H__CAE6C113_F4AA_4A9C_BA6B_C2EB975D8A59__INCLUDED_)
#define AFX_STDAFX_H__CAE6C113_F4AA_4A9C_BA6B_C2EB975D8A59__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000


// Insert your headers here
#define WIN32_LEAN_AND_MEAN		// Exclude rarely-used stuff from Windows headers

#include <process.h>

#include <afxwin.h>         // MFC core and standard components
#include <afxext.h>         // MFC extensions
#include <afxdisp.h>        // MFC OLE automation classes

#include <afxctl.h>
#include <comdef.h>

#ifndef _AFX_NO_AFXCMN_SUPPORT
#include <afxcmn.h>			// MFC support for Windows Common Controls
#endif // _AFX_NO_AFXCMN_SUPPORT

#include <afxconv.h>

#pragma comment(lib,"atl.lib")
#include <atldef.h>
#define _ATL_DLL_IMPL
#include <atliface.h>
#include <atlbase.h>

//You may derive a class from CComModule and use it if you want to override
//something, but do not change the name of _Module
extern CComModule _Module;
#include <atlcom.h>
#include <atlctl.h>  // For IObjectSafetyImpl

#include <exdisp.h>
#include <ExDispID.h>   // For WebBrowser and Internet Explorer 4.0x events.
#include <mshtmhst.h>




// For learning how to use SMART Pointers see
//    Dr. GUI on Components, COM, and ATL
//    Part 8: Get Smart! Using Our COM Object with Smart Pointers
// http://msdn.microsoft.com/library/default.asp?url=/library/en-us/dnguion/html/msdn_drguion020298.asp


// See 231931  PRB: Compiler Warnings Using #import on Mshtml.tlb
#pragma warning(disable : 4192)
#pragma warning(disable : 4049)
#pragma warning(disable : 4146)
#import <mshtml.tlb>
#import <shdocvw.dll>
#pragma warning(default: 4192)
#pragma warning(default: 4049)
#pragma warning(default: 4146)

#include <mshtmdid.h>

// TODO: reference additional headers your program requires here

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_STDAFX_H__CAE6C113_F4AA_4A9C_BA6B_C2EB975D8A59__INCLUDED_)
