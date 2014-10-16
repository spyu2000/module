#include<dlfcn.h>
#include "myalib.h"    

int main(int argc,char* argv[])
{

	void (*pTest)();  
    void *pdlHandle = dlopen("libDataCalc.so", RTLD_LAZY);  

    if(pdlHandle == NULL )    
	{
        printf("Failed load library\n");
        return -1;
    }

    char* pszErr = dlerror();
    if(pszErr != NULL)
    {
        printf("%s\n", pszErr);
        return -1;
    }   

    pTest = dlsym(pdlHandle, "test");
    pszErr = dlerror();
    if(pszErr != NULL)
    {
        printf("%s\n", pszErr);
        dlclose(pdlHandle);
        return -1;
    }   

    (*pTest)();
	dlclose(pdlHandle);
	return 0;  

}