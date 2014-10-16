//###########################################################################
//
//       Module:  GSMH.H
//
//  Decriptions:  This is header file of MicroDog Win32 API module.
//
//
//  Copyright (C) SafeNet China Ltd. All Rights Reserved.
//
//###########################################################################

#ifndef _GSMH_H_
#define _GSMH_H_


#ifdef  __cplusplus
extern "C" {
#endif


extern short int DogBytes,DogAddr;
extern unsigned long DogPassword;
extern unsigned long DogResult;
extern unsigned long NewPassword;
extern unsigned char DogCascade;
extern void * DogData;

extern unsigned long DogCheck(void);
extern unsigned long ReadDog(void);
extern unsigned long DogConvert(void);
extern unsigned long WriteDog(void);
extern unsigned long DisableShare(void);
extern unsigned long GetCurrentNo(void);


#ifdef  __cplusplus
}
#endif


#endif //_GSMH_H_
