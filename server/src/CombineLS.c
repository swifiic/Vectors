
/* Modified the source file so that we combine the comtent extracted before
 *
 * Abhishek Thakur @ BPHC - March 2018
 */
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <arpa/inet.h>

#define MAX_NAL 1024 * 1024
/*
-rw-rw-r-- 1 abhishek abhishek   3351 Mar 17 16:19 demo_0_1.out
-rw-rw-r-- 1 abhishek abhishek   1592 Mar 17 16:19 demo_0_2.out
-rw-rw-r-- 1 abhishek abhishek   1538 Mar 17 16:19 demo_0_3.out
-rw-rw-r-- 1 abhishek abhishek   1866 Mar 17 16:19 demo_0_4.out
-rw-rw-r-- 1 abhishek abhishek   2214 Mar 17 16:19 demo_0_5.out
-rw-rw-r-- 1 abhishek abhishek  20271 Mar 17 16:19 demo_1_1.out
-rw-rw-r-- 1 abhishek abhishek  12932 Mar 17 16:19 demo_1_2.out
-rw-rw-r-- 1 abhishek abhishek  13160 Mar 17 16:19 demo_1_3.out
-rw-rw-r-- 1 abhishek abhishek  24520 Mar 17 16:19 demo_1_4.out
-rw-rw-r-- 1 abhishek abhishek  28388 Mar 17 16:19 demo_1_5.out
-rw-rw-r-- 1 abhishek abhishek    674 Mar 17 16:19 demo.md
drwxrwxr-x 2 abhishek abhishek   4096 Mar 17 16:19 .
-rw-rw-r-- 1 abhishek abhishek      0 Mar 17 16:19 demo_0_0.out
-rw-rw-r-- 1 abhishek abhishek      0 Mar 17 16:19 demo_1_0.out
*/

typedef struct NalUnitHeader_s
{
  int nalUnitType;
  int nuhLayerId;
  int nuhTemporalIdPlus1;
} NalUnitHeader;


int findStartCodePrefix(FILE *inFile, int *numStartCodeZeros)
{
  int numZeros = 0;
  int currByte;
  
  for (;;)
  {
    currByte = fgetc(inFile);
    if (currByte == EOF)
    {
      return 0;
    }

    if (currByte == 0x01 && numZeros > 1)
    {
      *numStartCodeZeros = numZeros;
      return 1;
    }
    else if (currByte == 0)
    {
      numZeros++;
    }
    else
    {
      numZeros = 0;
    }
  }
}

int parseNalUnitHeader(FILE *inFile, NalUnitHeader *nalu)
{
  int byte0, byte1;

  byte0 = fgetc(inFile);
  byte1 = fgetc(inFile);

  if (byte0 == EOF || byte1 == EOF)
  {
    return 0;
  }

  nalu->nalUnitType = (byte0 >> 1) & 0x3f;
  nalu->nuhLayerId  = (((byte0 << 8) | byte1) >> 3) & 0x3f;
  nalu->nuhTemporalIdPlus1 = byte1 & 0x07;

  return 1;
}

void writeStartCodePrefixAndNUH(FILE *outFile, int numStartCodeZeros, NalUnitHeader *nalu)
{
  int byte0, byte1;
  int i;

  /* Start code prefix */
  if (numStartCodeZeros > 3)
  {
    numStartCodeZeros = 3;
  }
  for (i = 0; i < numStartCodeZeros; i++)
  {
    fputc(0, outFile);
  }
  fputc(0x01, outFile);

  /* NAL unit header */
  byte0 = ((nalu->nalUnitType << 6) | nalu->nuhLayerId) >> 5;
  byte1 = ((nalu->nuhLayerId << 3) | nalu->nuhTemporalIdPlus1) & 0xff;
  fputc(byte0, outFile);
  fputc(byte1, outFile);
}

int main(int argc, char **argv)
{
  FILE *outFile;
  FILE *inFile[8][8];
  FILE *inFileMD;
  int tIdMax = 6;

  int layerIdMax;
  int i, j;
  char * naluLayer = malloc(MAX_NAL);
  char * naluStartZeros = malloc(MAX_NAL);
  char * naluTemporal = malloc(MAX_NAL);
  char * naluType = malloc(MAX_NAL);
  int * naluSize = malloc(MAX_NAL*4);
  char * naluBuf = malloc(MAX_NAL*4);

  if (argc < 5 || argc > 5)
  {
    fprintf(stderr, "\n  Usage: CombineLS <infile_baseName> <outfile> <max temporal ID> <Max layer ID> \n\n");
    fprintf(stderr, "  Multiple output files created - one for each layer and temporal ID \n");
    exit(1);
  }

  outFile = fopen(argv[2], "wb");
  if (outFile == NULL)
  {
    fprintf(stderr, "Cannot open output file %s\n", argv[2]);
    exit(1);
  }

  char fileName[1024];
  snprintf(fileName, 1023, "%s.md", argv[1]);
  inFileMD = fopen(fileName, "rb");
  if (inFileMD == NULL)
  {
    fprintf(stderr, "Cannot open intput meta-data file %s\n", fileName);
    exit(1);
  }

  tIdMax = atoi(argv[3]);
  if (tIdMax < 0 || tIdMax > 6)
  {
    fprintf(stderr, "Invalid maximum temporal ID (must be in range 0-6)\n");
    exit(1);
  }

  layerIdMax = atoi(argv[4]);
  if (layerIdMax < 0 || layerIdMax > 7)
  {
    fprintf(stderr, "Invalid layer ID (must be in range 0-7)\n");
    exit(1);
  }

  for (i = 0 ; i < layerIdMax; i++)
  {
    for (j = 1 ; j <= tIdMax ; j ++) {
        snprintf(fileName, 1023, "%s_L%dT%d.out", argv[1], i , j);
        inFile[i][j] = fopen(fileName, "rb");
    }
  }
  if(NULL == inFile[0][1]) {
    fprintf(stderr, "Cannot open base intput file %s_0_1.out\n", argv[1]);
    exit(1);
  }
  
  uint32_t netOrder ;
  if(!(fread(&netOrder, sizeof(netOrder), 1, inFileMD)>0))
      printf("fread read netOrder failed. \n");
  int naluCount = ntohl(netOrder);
  printf("Processing for maxNalu=%d\n", naluCount);

  if(!(fread(naluLayer, 1, naluCount, inFileMD)>0))
      printf("fread read naluLayer failed. \n")
          ;
  if(!(fread(naluTemporal, 1, naluCount, inFileMD)>0))
      printf("fread read naluTemporal failed. \n");

  if(!(fread(naluStartZeros, 1, naluCount, inFileMD)>0))
      printf("fread read naluStartZeros failed. \n");

  if(!(fread(naluType, 1, naluCount, inFileMD)>0))
      printf("fread read naluType failed. \n");

  if(!(fread(naluSize, 4, naluCount, inFileMD)>0))
      printf("fread read naluSize failed. \n");

  fclose(inFileMD);

  /* Iterate through all NAL units */
  for (i=0;i < naluCount;i++)
  {
    char c = 0;
    int maxIdx = naluSize[i] + naluStartZeros[i] + 1 + 2;  // Start CODE is zeroes followed by one byte of 01; NALU header 2 bytes
    printf("Combining for %d layer=%i tId+1=%i zeros=%i typeCode=%i size=%i\n", i, naluLayer[i], naluTemporal[i], naluStartZeros[i], naluType[i], naluSize[i]);
    if(NULL == inFile[(int)naluLayer[i]][(int)naluTemporal[i]]) {
        printf("Inserting dummy values for %d\n", maxIdx);
        int k;
        for(k=0; k < maxIdx; k++){
             fputc(c, outFile);
        }
    } else {
        if(fread(naluBuf, 1, maxIdx, inFile[(int)naluLayer[i]][(int)naluTemporal[i]]) != maxIdx) {
	     fprintf(stderr, "Read EOF for i=%d and maxIdx=%d\n", i, maxIdx); 
        }
        fwrite(naluBuf, 1, maxIdx, outFile);
    }
  }
  
  fclose(outFile);
  for (i = 0 ; i < layerIdMax; i++) {
    for (j = 1 ; j <= tIdMax ; j ++) {
        if(NULL!= inFile[i][j])
             fclose(inFile[i][j]);
    }
  }


  return 0;
}
