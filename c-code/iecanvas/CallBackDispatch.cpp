
#include "stdafx.h"
#include "CallBackDispatch.h"

// Hardcoded information for extending the Object Model 

CString csz_OneParamCallBack = "oneParamCallBack";
CString csz_TwoParamCallBack = "twoParamCallBack";

#define DISPID_OneParamCallBack 1
#define DISPID_TwoParamCallBack 2

CCallBackDispatch::CCallBackDispatch(JavaVM *pJavaVM, jobject pObj)
{
    m_cRef = 0;
    m_pObj = pObj;
    m_pJavaVM = pJavaVM;
}

CCallBackDispatch::~CCallBackDispatch( void )
{
	ASSERT( m_cRef == 0 );
}


STDMETHODIMP CCallBackDispatch::QueryInterface( REFIID riid, void **ppv )
{
   *ppv = NULL;
   if ( IID_IDispatch == riid )
   {
      *ppv = this;
   }	
	if ( NULL != *ppv )
   {
      ((LPUNKNOWN)*ppv)->AddRef();
      return NOERROR;
   }
	return E_NOINTERFACE;
}


STDMETHODIMP_(ULONG) CCallBackDispatch::AddRef(void)
{
    return ++m_cRef;
}

STDMETHODIMP_(ULONG) CCallBackDispatch::Release(void)
{
    return --m_cRef;
}


//IDispatch
STDMETHODIMP CCallBackDispatch::GetTypeInfoCount(UINT* /*pctinfo*/)
{
	return E_NOTIMPL;
}

STDMETHODIMP CCallBackDispatch::GetTypeInfo(
            /* [in] */ UINT /*iTInfo*/,
            /* [in] */ LCID /*lcid*/,
            /* [out] */ ITypeInfo** /*ppTInfo*/)
{
	return E_NOTIMPL;
}



STDMETHODIMP CCallBackDispatch::GetIDsOfNames(
            /* [in] */ REFIID riid,
            /* [size_is][in] */ OLECHAR** rgszNames,
            /* [in] */ UINT cNames,
            /* [in] */ LCID lcid,
            /* [size_is][out] */ DISPID* rgDispId)
{
	HRESULT hr;
	UINT	i;

	// Assume some degree of success
	hr = NOERROR;


	for ( i=0; i < cNames; i++) {
		CString cszName  = rgszNames[i];

		if(cszName == csz_OneParamCallBack)
		{
			rgDispId[i] = DISPID_OneParamCallBack;
		}
		else if(cszName == csz_TwoParamCallBack)
		{
			rgDispId[i] = DISPID_TwoParamCallBack;
		}
		else {
			// One or more are unknown so set the return code accordingly
			hr = ResultFromScode(DISP_E_UNKNOWNNAME);
			rgDispId[i] = DISPID_UNKNOWN;
		}
	}
	return hr;
}

STDMETHODIMP CCallBackDispatch::Invoke(
            /* [in] */ DISPID dispIdMember,
            /* [in] */ REFIID /*riid*/,
            /* [in] */ LCID /*lcid*/,
            /* [in] */ WORD wFlags,
            /* [out][in] */ DISPPARAMS* pDispParams,
            /* [out] */ VARIANT* pVarResult,
            /* [out] */ EXCEPINFO* /*pExcepInfo*/,
            /* [out] */ UINT* puArgErr)
{
   USES_CONVERSION;

   switch (dispIdMember)
	{
   case DISPID_OneParamCallBack:
      {
		   if ( wFlags & DISPATCH_METHOD )
		   {
			   //arguments come in reverse order
			   //for some reason

            VARIANT *var = &pDispParams->rgvarg[0];
            LPTSTR param1 = OLE2T(V_BSTR(var));


	         // Attach this thread.
	         JNIEnv *pEnv	= NULL;
	         m_pJavaVM->AttachCurrentThread((void **)&pEnv,NULL);

            jstring param1Str = pEnv->NewStringUTF(param1);

            jstring returnVal = pEnv->NewStringUTF("");

            if (pEnv && m_pObj) {
		         jclass clazz = pEnv->GetObjectClass(m_pObj);
		         if (clazz) {
			         jmethodID mid = pEnv->GetMethodID(clazz, "oneParamCallBack", "(Ljava/lang/String;)Ljava/lang/String;");
			         if (mid) {
				         jstring r = (jstring)pEnv->CallObjectMethod(m_pObj, mid, param1Str);
                     if (r != NULL)
                        returnVal = r;
			         }
		         }
	         }

            const char *retChars =  pEnv->GetStringUTFChars(returnVal, NULL);
            _bstr_t retBstr = retChars;
            pEnv->ReleaseStringUTFChars(returnVal, retChars);

            //if return value allocated
            if (pVarResult)
               VariantInit(pVarResult);
            else
               pVarResult = new VARIANT;
            V_VT(pVarResult) = VT_BSTR;;
				V_BSTR(pVarResult) = retBstr;


         }
      }
      break;   
   case DISPID_TwoParamCallBack:
      {
		   if ( wFlags & DISPATCH_METHOD )
		   {
			   //arguments come in reverse order
			   //for some reason

            VARIANT *var = &pDispParams->rgvarg[1];
            LPTSTR _param1 = OLE2T(V_BSTR(var));  
			int wlen1 = 0;   
			WCHAR* param1 = NULL;
			if (_param1 != NULL){       
			  int clen = lstrlen(_param1); 
			  wlen1 = clen+1;   
			  param1 = new WCHAR[wlen1];  
			  wlen1 = ::MultiByteToWideChar(CP_ACP,NULL,_param1,clen,param1,wlen1);
			}

            var = &pDispParams->rgvarg[0];
            LPTSTR _param2 = OLE2T(V_BSTR(var));
			WCHAR* param2 = NULL;  
			int wlen2 = 0;
			if (_param2 != NULL){       
			  int clen = lstrlen(_param2);  
			  wlen2 = clen+1;
			  param2 = new WCHAR[wlen2];  
			  wlen2 = ::MultiByteToWideChar(CP_ACP,NULL,_param2,clen,param2,wlen2);
			}


	         // Attach this thread.
	         JNIEnv *pEnv	= NULL;
	         m_pJavaVM->AttachCurrentThread((void **)&pEnv,NULL);

            jstring param1Str = pEnv->NewString(param1,wlen1);
            jstring param2Str = pEnv->NewString(param2,wlen2);

			if(param1 != NULL){
				delete param1;
			}
			if(param2 != NULL){
				delete param2;
			}

            jstring returnVal = pEnv->NewStringUTF("");

            if (pEnv && m_pObj) {
		         jclass clazz = pEnv->GetObjectClass(m_pObj);
		         if (clazz) {
			         jmethodID mid = pEnv->GetMethodID(clazz, "twoParamCallBack", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
			         if (mid) {
				         jstring r = (jstring)pEnv->CallObjectMethod(m_pObj, mid, param1Str, param2Str);
                     if (r != NULL)
                        returnVal = r;
			         }
		         }
	         }

            const char *retChars =  pEnv->GetStringUTFChars(returnVal, NULL);
            _bstr_t retBstr = retChars;
            pEnv->ReleaseStringUTFChars(returnVal, retChars);

            //if return value allocated
            if (pVarResult)
               VariantInit(pVarResult);
            else
               pVarResult = new VARIANT;
            V_VT(pVarResult) = VT_BSTR;;
				V_BSTR(pVarResult) = retBstr;



         }
      }
      break;   
   default:
      break;
	}
   
	return S_OK;
}


