#ifndef __CALLBACKDISPATCH_H__
#define __CALLBACKDISPATCH_H__

#pragma once

#include "jni.h"


class CCallBackDispatch : public IDispatch	
{
protected:
   ULONG               m_cRef;

public:
	CCallBackDispatch (JavaVM *pJavaVM, jobject pObj);
	~CCallBackDispatch ();

private: 
	jobject		m_pObj;
	JavaVM		*m_pJavaVM;


public:

   // CViewImplDispatch
   STDMETHODIMP QueryInterface(REFIID, void **);
   STDMETHODIMP_(ULONG) AddRef(void);
   STDMETHODIMP_(ULONG) Release(void);

   //IDispatch
   STDMETHODIMP GetTypeInfoCount(UINT* pctinfo);
   STDMETHODIMP GetTypeInfo(
      /* [in] */ UINT iTInfo,
      /* [in] */ LCID lcid,
      /* [out] */ ITypeInfo** ppTInfo);
   STDMETHODIMP GetIDsOfNames(
      /* [in] */ REFIID riid,
      /* [size_is][in] */ LPOLESTR *rgszNames,
      /* [in] */ UINT cNames,
      /* [in] */ LCID lcid,
      /* [size_is][out] */ DISPID *rgDispId);
   STDMETHODIMP Invoke(
      /* [in] */ DISPID dispIdMember,
      /* [in] */ REFIID riid,
      /* [in] */ LCID lcid,
      /* [in] */ WORD wFlags,
      /* [out][in] */ DISPPARAMS  *pDispParams,
      /* [out] */ VARIANT  *pVarResult,
      /* [out] */ EXCEPINFO *pExcepInfo,
      /* [out] */ UINT *puArgErr);


};



#endif // __CALLBACKDISPATCH_H__