// Registry.h: interface for the Registry class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_REGISTRY_H__1C0CEF00_CDCD_40E0_9045_7DCE587A0C85__INCLUDED_)
#define AFX_REGISTRY_H__1C0CEF00_CDCD_40E0_9045_7DCE587A0C85__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "jni.h"

class ObjMap;

extern jint getHashCode(JNIEnv* pEnv, jobject aObj);

class Registry {
public:

	static void		Add(void* key, void*& value);
	static int		Remove(void *key);
	static int		Lookup(void *key, void*& value);
	static jint		GetHashCode(JNIEnv* pEnv, jobject aObj);

protected:
	Registry() { };
	~Registry();

private:
	static ObjMap*	_map;
};

#endif // !defined(AFX_REGISTRY_H__1C0CEF00_CDCD_40E0_9045_7DCE587A0C85__INCLUDED_)
