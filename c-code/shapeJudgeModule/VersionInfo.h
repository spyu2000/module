#if !defined(AFX_VERSIONINFO_H__24C6991D_A062_4095_B98B_ED32A87682EC__INCLUDED_)
#define AFX_VERSIONINFO_H__24C6991D_A062_4095_B98B_ED32A87682EC__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#define _LINUX  //定义Linux版本时 开去此宏
#ifndef _LINUX
	#define long8 __int64
#else
	#define long8 long long
#endif

#endif // !defined(AFX_THREAD_H__24C6991D_A062_4095_B98B_ED32A87682EC__INCLUDED_)
