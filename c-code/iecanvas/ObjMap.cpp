// ObjMap.cpp: implementation of the ObjMap class.
//
//////////////////////////////////////////////////////////////////////

#include "stdafx.h"
#include "ObjMap.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

void ObjMap::free() {
	delete _map;
}

ObjMap::ObjMap(int gran) {
	_map = new CMapPtrToPtr(gran);
}

void ObjMap::add(void* key, void*& value) {
	_map->SetAt(key, value);
}

int ObjMap::remove(void* key) {
	void *value;
	_map->Lookup(key, value);
	delete value;
	return _map->RemoveKey(key);
}

int ObjMap::lookup(void *key, void*& value) {
	return (int)_map->Lookup(key, value);
}