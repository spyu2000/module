// Registry.cpp: implementation of the Registry class.
//
//////////////////////////////////////////////////////////////////////

#include "stdafx.h"
#include "Registry.h"
#include "ObjMap.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

ObjMap* Registry::_map = NULL;



Registry::~Registry() {
	_map->free();
}

jint Registry::GetHashCode(JNIEnv* pEnv, jobject aObj) {
	jclass clazz = pEnv->GetObjectClass(aObj);
	jmethodID hcMID = pEnv->GetMethodID(clazz, "hashCode", "()I");
	jint hashCode = pEnv->CallIntMethod(aObj, hcMID);
	return hashCode;
}

void Registry::Add(void* key, void*& value) {
	if (_map == NULL)
		_map = new ObjMap(10);
	_map->add(key, value);
}

int Registry::Remove(void* key) {
	if (_map == NULL)
		return FALSE;
	return _map->remove(key);
}

int Registry::Lookup(void* key, void*& value) {
	if (_map == NULL)
		return FALSE;
	return _map->lookup(key, value);
}