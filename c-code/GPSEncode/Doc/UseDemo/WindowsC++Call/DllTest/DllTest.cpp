// DllTest.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include <windows.h>
  
typedef bool( __stdcall *lpEncoderFun)(double loIn, double laIn,double& loOut,double& laOut);
typedef bool( __stdcall *lpDecoderFun)(double loIn, double laIn,double& loOut,double& laOut);

int main(int argc, char* argv[])
{
	printf("start test!\n");
	HINSTANCE hDll;
	lpEncoderFun encoderFun;
	lpDecoderFun decoderFun;
	hDll = LoadLibrary("..\\Release\\DataCalculate.dll");
	if (hDll != NULL)
	{
		encoderFun = (lpEncoderFun)GetProcAddress(hDll, "encode");//MAKEINTRESOURCE(2));
		decoderFun = (lpDecoderFun)GetProcAddress(hDll, "decode");//MAKEINTRESOURCE(3));

		if (encoderFun != NULL)
		{
			double lo = 121.12345678;
			double la = 31.12345678;
			double loOut = 0;
			double laOut = 0;

			DWORD t=::GetTickCount();
			for(int i=0;i<1000000;i++){
				encoderFun(lo,la,loOut,laOut);
			}

			printf("time=%d\n", (::GetTickCount()-t));
		}

		FreeLibrary(hDll);
	}
    
	return 0;
}

