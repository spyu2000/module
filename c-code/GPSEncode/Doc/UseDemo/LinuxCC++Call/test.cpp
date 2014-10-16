#include <dlfcn.h>
#include <iostream>
#include <time.h>

using namespace std;

int main(int argc, char* argv[])
{
    char *p;    
    void *lib;
    lib = dlopen("./libDataCal.so", RTLD_LAZY);
    if (lib == NULL)
    {
        cout << "NULL" << endl;
        p = dlerror();
        cout <<"load err"<< p << endl;
    }
    else
    {
        cout <<"load succ "<< std::hex << lib << endl;
    }

    typedef bool (*encodeFun)(double lo,double la, double& loOut, double& laOut);
    encodeFun encode = (encodeFun)dlsym(lib, "encode");
    
    double lo = 121.12345678;
    double la = 31.12345678;
    double loOut = 0;
    double laOut = 0;
    
    int seconds = time((time_t*)NULL);
    cout<<"star..."<<seconds<<endl;
    for(int i=0;i<1000000;i++)
    {
		encode(lo,la,loOut,laOut);
    }
    seconds = time((time_t*)NULL);
    cout<<" end..."<<seconds<<endl;
    return 0;
}

