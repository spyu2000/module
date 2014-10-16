// ObjMap.h: interface for the ObjMap class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_OBJMAP_H__AE7EF019_6A4C_4C90_B34F_38E61C2BEECC__INCLUDED_)
#define AFX_OBJMAP_H__AE7EF019_6A4C_4C90_B34F_38E61C2BEECC__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include <afxcoll.h>

class ObjMap {

public:
	ObjMap(int gran);

	void			add(void* key, void*& value);
	int				remove(void* key);
	int				lookup(void* key, void*& value);
	void			free();

private:
	CMapPtrToPtr*	_map;
};

#endif // !defined(AFX_OBJMAP_H__AE7EF019_6A4C_4C90_B34F_38E61C2BEECC__INCLUDED_)
