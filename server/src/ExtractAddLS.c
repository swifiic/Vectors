#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <arpa/inet.h>

/* at 30 fps and 8 layers (temporal * layerId) around 250 NALU per second
 * 1 Million NALU should handle one hour video chunk */
#define MAX_NAL 1024 * 1024

enum NalUnitType
{
  NAL_UNIT_CODED_SLICE_TRAIL_N = 0, NAL_UNIT_CODED_SLICE_TRAIL_R,

  NAL_UNIT_CODED_SLICE_TSA_N, NAL_UNIT_CODED_SLICE_TSA_R,

  NAL_UNIT_CODED_SLICE_STSA_N, NAL_UNIT_CODED_SLICE_STSA_R,

  NAL_UNIT_CODED_SLICE_RADL_N, NAL_UNIT_CODED_SLICE_RADL_R,

  NAL_UNIT_CODED_SLICE_RASL_N, NAL_UNIT_CODED_SLICE_RASL_R,

  NAL_UNIT_RESERVED_VCL_N10, NAL_UNIT_RESERVED_VCL_R11,
  NAL_UNIT_RESERVED_VCL_N12, NAL_UNIT_RESERVED_VCL_R13,
  NAL_UNIT_RESERVED_VCL_N14, NAL_UNIT_RESERVED_VCL_R15,

  NAL_UNIT_CODED_SLICE_BLA_W_LP, NAL_UNIT_CODED_SLICE_BLA_W_RADL,
  NAL_UNIT_CODED_SLICE_BLA_N_LP, NAL_UNIT_CODED_SLICE_IDR_W_RADL,
  NAL_UNIT_CODED_SLICE_IDR_N_LP, NAL_UNIT_CODED_SLICE_CRA,
  NAL_UNIT_RESERVED_IRAP_VCL22, NAL_UNIT_RESERVED_IRAP_VCL23,

  NAL_UNIT_RESERVED_VCL24, NAL_UNIT_RESERVED_VCL25, NAL_UNIT_RESERVED_VCL26, NAL_UNIT_RESERVED_VCL27,
  NAL_UNIT_RESERVED_VCL28, NAL_UNIT_RESERVED_VCL29, NAL_UNIT_RESERVED_VCL30, NAL_UNIT_RESERVED_VCL31,

  NAL_UNIT_VPS, NAL_UNIT_SPS,
  NAL_UNIT_PPS, NAL_UNIT_ACCESS_UNIT_DELIMITER,
  NAL_UNIT_EOS, NAL_UNIT_EOB,
  NAL_UNIT_FILLER_DATA, NAL_UNIT_PREFIX_SEI,
  NAL_UNIT_SUFFIX_SEI, NAL_UNIT_RESERVED_NVCL41,
  NAL_UNIT_RESERVED_NVCL42, NAL_UNIT_RESERVED_NVCL43,
  NAL_UNIT_RESERVED_NVCL44, NAL_UNIT_RESERVED_NVCL45, NAL_UNIT_RESERVED_NVCL46, NAL_UNIT_RESERVED_NVCL47,
  NAL_UNIT_UNSPECIFIED_48, NAL_UNIT_UNSPECIFIED_49, NAL_UNIT_UNSPECIFIED_50, NAL_UNIT_UNSPECIFIED_51,
  NAL_UNIT_UNSPECIFIED_52, NAL_UNIT_UNSPECIFIED_53, NAL_UNIT_UNSPECIFIED_54, NAL_UNIT_UNSPECIFIED_55,
  NAL_UNIT_UNSPECIFIED_56, NAL_UNIT_UNSPECIFIED_57, NAL_UNIT_UNSPECIFIED_58, NAL_UNIT_UNSPECIFIED_59,
  NAL_UNIT_UNSPECIFIED_60, NAL_UNIT_UNSPECIFIED_61, NAL_UNIT_UNSPECIFIED_62, NAL_UNIT_UNSPECIFIED_63,
  NAL_UNIT_INVALID,
};

char *nalUnitNames[] =
{
  "NAL_UNIT_CODED_SLICE_TRAIL_N", "NAL_UNIT_CODED_SLICE_TRAIL_R",

  "NAL_UNIT_CODED_SLICE_TSA_N", "NAL_UNIT_CODED_SLICE_TSA_R",

  "NAL_UNIT_CODED_SLICE_STSA_N", "NAL_UNIT_CODED_SLICE_STSA_R",

  "NAL_UNIT_CODED_SLICE_RADL_N", "NAL_UNIT_CODED_SLICE_RADL_R",

  "NAL_UNIT_CODED_SLICE_RASL_N", "NAL_UNIT_CODED_SLICE_RASL_R",

  "NAL_UNIT_RESERVED_VCL_N10", "NAL_UNIT_RESERVED_VCL_R11",
  "NAL_UNIT_RESERVED_VCL_N12", "NAL_UNIT_RESERVED_VCL_R13",
  "NAL_UNIT_RESERVED_VCL_N14", "NAL_UNIT_RESERVED_VCL_R15",

  "NAL_UNIT_CODED_SLICE_BLA_W_LP", "NAL_UNIT_CODED_SLICE_BLA_W_RADL",
  "NAL_UNIT_CODED_SLICE_BLA_N_LP", "NAL_UNIT_CODED_SLICE_IDR_W_RADL",
  "NAL_UNIT_CODED_SLICE_IDR_N_LP", "NAL_UNIT_CODED_SLICE_CRA",
  "NAL_UNIT_RESERVED_IRAP_VCL22", "NAL_UNIT_RESERVED_IRAP_VCL23",

  "NAL_UNIT_RESERVED_VCL24", "NAL_UNIT_RESERVED_VCL25", "NAL_UNIT_RESERVED_VCL26", "NAL_UNIT_RESERVED_VCL27",
  "NAL_UNIT_RESERVED_VCL28", "NAL_UNIT_RESERVED_VCL29", "NAL_UNIT_RESERVED_VCL30", "NAL_UNIT_RESERVED_VCL31",

  "NAL_UNIT_VPS", "NAL_UNIT_SPS",
  "NAL_UNIT_PPS", "NAL_UNIT_ACCESS_UNIT_DELIMITER",
  "NAL_UNIT_EOS", "NAL_UNIT_EOB",
  "NAL_UNIT_FILLER_DATA", "NAL_UNIT_PREFIX_SEI",
  "NAL_UNIT_SUFFIX_SEI", "NAL_UNIT_RESERVED_NVCL41",
  "NAL_UNIT_RESERVED_NVCL42", "NAL_UNIT_RESERVED_NVCL43",
  "NAL_UNIT_RESERVED_NVCL44", "NAL_UNIT_RESERVED_NVCL45", "NAL_UNIT_RESERVED_NVCL46", "NAL_UNIT_RESERVED_NVCL47",
  "NAL_UNIT_UNSPECIFIED_48", "NAL_UNIT_UNSPECIFIED_49", "NAL_UNIT_UNSPECIFIED_50", "NAL_UNIT_UNSPECIFIED_51",
  "NAL_UNIT_UNSPECIFIED_52", "NAL_UNIT_UNSPECIFIED_53", "NAL_UNIT_UNSPECIFIED_54", "NAL_UNIT_UNSPECIFIED_55",
  "NAL_UNIT_UNSPECIFIED_56", "NAL_UNIT_UNSPECIFIED_57", "NAL_UNIT_UNSPECIFIED_58", "NAL_UNIT_UNSPECIFIED_59",
  "NAL_UNIT_UNSPECIFIED_60", "NAL_UNIT_UNSPECIFIED_61", "NAL_UNIT_UNSPECIFIED_62", "NAL_UNIT_UNSPECIFIED_63"
};

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

int extractor(char *inFileStr, char *outBase, char *temporalIdStr, char* mxLayerStr) {
  FILE *inFile;        // Input bitstream file.
  FILE *outFile[8][8]; // Used for storing output bitstream files.
  FILE *outFileMD;     // File for storing meta data which is used while combining.
  int tIdMax = 6;
  NalUnitHeader nalu;
  int numStartCodeZeros;

  int layerIdMax;
  int i, j;

  char nalName[32];
  char * naluLayer = malloc(MAX_NAL);
  char * naluStartZeros = malloc(MAX_NAL);
  char * naluTemporal = malloc(MAX_NAL);
  char * naluType = malloc(MAX_NAL);
  int * naluSize = malloc(MAX_NAL*4);
  int naluCount = 0;

  inFile = fopen(inFileStr, "rb");
  if (inFile == NULL)
  {
    fprintf(stderr, "Cannot open input file %s\n", inFileStr);
    exit(1);
  }

  char fileName[1024];
  snprintf(fileName, 1023, "%s.md", outBase);
  outFileMD = fopen(fileName, "wb");
  if (outFileMD == NULL)
  {
    fprintf(stderr, "Cannot open output meta-data file %s\n", fileName);
    exit(1);
  }

  tIdMax = atoi(temporalIdStr);
  if (tIdMax < 0 || tIdMax > 6)
  {
    fprintf(stderr, "Invalid maximum temporal ID (must be in range 0-6)\n");
    exit(1);
  }

  layerIdMax = atoi(mxLayerStr);
  if (layerIdMax < 0 || layerIdMax > 7)
  {
    fprintf(stderr, "Invalid layer ID (must be in range 0-7)\n");
    exit(1);
  }

  for (i = 0 ; i < layerIdMax; i++)
  {
    for (j = 1 ; j <= tIdMax ; j ++) {
        snprintf(fileName, 1023, "%s_L%dT%d.out", outBase, i , j);
        outFile[i][j] = fopen(fileName, "wb");
    }
  }

  printf("Decoding with layers max as %d and temporal max as %d\n", layerIdMax, tIdMax);

  /* Iterate through all NAL units */
  for (;;)
  {
    if (!findStartCodePrefix(inFile, &numStartCodeZeros))
    {
      break;
    }
    if (!parseNalUnitHeader(inFile, &nalu))
    {
      break;
    }
    naluLayer[naluCount] = nalu.nuhLayerId;
    naluStartZeros[naluCount] = numStartCodeZeros;
    naluTemporal[naluCount] = nalu.nuhTemporalIdPlus1;
    naluType[naluCount] = nalu.nalUnitType;


      /* Write current NAL unit to output bitstream */

      long naluBytesStartPos;
      long numNaluBytes;

      printf("Keep  ");

      writeStartCodePrefixAndNUH(outFile[(int)naluLayer[naluCount]][(int)naluTemporal[naluCount]], numStartCodeZeros, &nalu);

      naluBytesStartPos = ftell(inFile);
      /* Find beginning of the next NAL unit to calculate length of the current unit */
      if (findStartCodePrefix(inFile, &numStartCodeZeros))
      {
        numNaluBytes = ftell(inFile) - naluBytesStartPos - numStartCodeZeros - 1;
      }
      else
      {
        numNaluBytes = ftell(inFile) - naluBytesStartPos;
        printf("Did not find start Code prefix - possibly EOF\n");
      }
      fseek(inFile, naluBytesStartPos, SEEK_SET);
      naluSize[naluCount] = numNaluBytes;

      i = 0;

      // if (numLayerIds == 1 && nalu.nalUnitType == NAL_UNIT_VPS)
      // {
      //   char nalByte = fgetc(inFile);
      //   nalByte = nalByte | (0x0C);  // set vps_max_layers_minus1 bits(5-4) to 0 for HM decoder
      //   nalByte = nalByte & ~(0x03); // set vps_max_layers_minus1 bits(3-0) to 0 for HM decoder
      //   fputc(nalByte, outFile); 
      //   i++;
      //   nalByte = fgetc(inFile);
      //   nalByte = nalByte & ~(0xF0);
      //   fputc(nalByte, outFile); 
      //   i++;
      // }
      // else if (numLayerIds > 1 && nalu.nalUnitType == NAL_UNIT_VPS)
      // {
      //   /* sub-bitstream extraction process for additional layer sets */
      //   int nalByte = fgetc(inFile);
      //   nalByte = nalByte & ~(0x04);  /* vps_base_layer_available_flag in each VPS is set equal to 0 */
      //   fputc(nalByte, outFile);
      //   i++;
      // }

      for (; i < numNaluBytes; i++)
      {
        fputc(fgetc(inFile), outFile[(int)naluLayer[naluCount]][(int)naluTemporal[naluCount]]);
      }

    strcpy(nalName, nalUnitNames[nalu.nalUnitType]);
    for (i = strlen(nalName); i < 31; i++)
    {
      nalName[i] = ' ';
    }
    nalName[31] = '\0';

    printf("%s  %d layer ID: %i  temporal ID+1: %i zeros: %i typeCode: %i size:%i\n", nalName, naluCount, naluLayer[naluCount], naluTemporal[naluCount], naluStartZeros[naluCount], naluType[naluCount], naluSize[naluCount]);
    naluCount++;
  }
  
  printf("Terminating by closing files naluCount=%d\n", naluCount);
  printf("Layers max as %d and temporal max as %d\n", layerIdMax, tIdMax);
  fclose(inFile);
  for (i = 0 ; i < layerIdMax; i++) {
    for (j = 1 ; j <= tIdMax ; j ++) {
        fclose(outFile[i][j]);
    }
  }
  uint32_t netOrder = htonl(naluCount);
  fwrite(&netOrder, sizeof(netOrder), 1, outFileMD);

  fwrite(naluLayer, 1, naluCount, outFileMD);
  fwrite(naluTemporal, 1, naluCount, outFileMD);
  fwrite(naluStartZeros, 1, naluCount, outFileMD);
  fwrite(naluType, 1, naluCount, outFileMD);
  fwrite(naluSize, 4, naluCount, outFileMD);

  fclose(outFileMD);


  return 0;
  
}

int main(int argc, char **argv)
{
  if (argc < 5 || argc > 5)
  {
    fprintf(stderr, "\n  Usage: ExtractAddLSWithMD <infile> <outfile_baseName> <max temporal ID+1> <Max layer ID> \n\n");
    fprintf(stderr, "  Multiple output files created - one for each layer and temporal ID \n");
    exit(1);
  }
  extractor(argv[1], argv[2], argv[3], argv[4]);
  return 0;
}
