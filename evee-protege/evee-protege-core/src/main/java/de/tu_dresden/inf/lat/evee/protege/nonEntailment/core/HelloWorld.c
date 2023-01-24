#include "de_tu_dresden_inf_lat_evee_protege_nonEntailment_core_NonEntailmentViewComponent.h"
#include <unistd.h>

JNIEXPORT jstring JNICALL Java_de_tu_1dresden_inf_lat_evee_protege_nonEntailment_core_NonEntailmentViewComponent_getHelloWorld
  (JNIEnv *env, jclass cls)
{
    FILE *fp;
    fp = fopen ("E:\\Programs\\Protege\\Protege-5.5.0\\NewlyCreatedDirectory\\NewlyCreatedFile.txt", "w");
    fclose (fp);
    char *name = "Hello World!";
    return (*env)->NewStringUTF(env, name);
}
