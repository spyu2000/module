
#define VER_MARJOR  4
#define VER_MINOR   010
#define PART_NO_STR  "MICRODOG-LINUX-JAVA\0"

typedef struct _StruVersion
{
  char VersionStr[10];
  int MajorVer;
  int MinorVer;
  char PartNumber[30];
}
StruVersion;

static StruVersion svjava=
{
	{'V','e','r','s','i','o','n','1','2','3'},
	VER_MARJOR, VER_MINOR, {PART_NO_STR}
};


