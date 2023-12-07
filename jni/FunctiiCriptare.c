#include<jni.h>
#include <stdio.h>
#include <iostream>

jstring Java_com_example_secondtry_kotlin_ControlActivity_Encrypt(JNIENV* env,jobect obj)
unsigned char sBoxLayer[16] = { 0xC, 0x5, 0x6, 0xB, 0x9, 0x0, 0xA, 0xD, 0x3, 0xE, 0xF, 0x8, 0x4, 0x7, 0x1, 0x2 };

unsigned char sBoxLayerInv[16] = { 0x5, 0xE, 0xF, 0x8, 0xC, 0x1, 0x2, 0xD, 0xB, 0x4, 0x6, 0x3, 0x0, 0x7, 0x9, 0xA };

void copyKey(const unsigned char *from, unsigned char *to, const unsigned char keyLen) {
	int i;
	for (i = 0; i < keyLen; i++) {
		to[i] = from[i];
	}
}
void generateRoundKeys(const unsigned char *suppliedKey, unsigned char keys[32][8]) {
	// trashable key copies
	unsigned char key[16];
	unsigned char newKey[16];
	unsigned char i, j;
	copyKey(suppliedKey, key, 16);
	copyKey(key, keys[0], 8);
	for (i = 1; i < 32; i++) {
		// rotate left 61 bits
		for (j = 0; j <16; j++) {
			newKey[j] = (key[(j + 7) % 16] << 5) | (key[(j + 8) % 16] >> 3);
		}
		copyKey(newKey, key, 16);

		// pass leftmost 8-bits through sBoxes
		key[0] = (sBoxLayer[key[0] >> 4] << 4) | (sBoxLayer[key[0] & 0xF]);

		// xor roundCounter into bits 62 through 66
		key[8] ^= i << 6; // bits 63-62
		key[7] ^= i >> 2; // bits 66-64

		copyKey(key, keys[i], 8);
	}
}
void addRoundKey(unsigned char *block, unsigned char *roundKey) {
	unsigned char i;
	for (i = 0; i < 8; i++) {
		block[i] ^= roundKey[i];
	}
}

void pLayer(unsigned char *block) {
	unsigned char i, j, indexVal, andVal;
	unsigned char initial[8];
	copyKey(block, initial, 8);
	for (i = 0; i <8; i++) {
		block[i] = 0;
		for (j = 0; j < 8; j++) {
			indexVal = 4 * (i % 2) + (3 - (j >> 1));
			andVal = (8 >> (i >> 1)) << ((j % 2) << 2);
			block[i] |= ((initial[indexVal] & andVal) != 0) << j;
		}
	}
}
void pLayerInv(unsigned char *block) {
	unsigned char i, j, indexVal, andVal;
	unsigned char initial[8];
	copyKey(block, initial, 8);
	for (i = 0; i < 8; i++) {
		block[i] = 0;
		for (j = 0; j < 8; j++) {
			indexVal = (7 - ((2 * j) % 8)) - (i < 4);
			andVal = (7 - ((2 * i) % 8)) - (j < 4);
			block[i] |= ((initial[indexVal] & (1 << andVal)) != 0) << j;
		}
	}
}
void encrypt(unsigned char *block, const unsigned char *key) {
	unsigned char roundKeys[32][8];
	unsigned char i, j;
	generateRoundKeys(key, roundKeys);
	for (i = 0; i < 32 - 1; i++) {
		addRoundKey(block, roundKeys[i]);
		for (j = 0; j < 8; j++) {
			block[j] = (sBoxLayer[block[j] >> 4] << 4) | sBoxLayer[block[j] & 0xF];
		}
		pLayer(block);
	}
	addRoundKey(block, roundKeys[32 - 1]);
}
void decrypt(unsigned char *block, const unsigned char *key) {
	unsigned char roundKeys[32][8];
	unsigned char i, j;
	generateRoundKeys(key, roundKeys);
	for (i = 32 - 1; i > 0; i--) {
		addRoundKey(block, roundKeys[i]);
		pLayerInv(block);
		for (j = 0; j < 8; j++) {
			block[j] = (sBoxLayerInv[block[j] >> 4] << 4) | sBoxLayerInv[block[j] & 0xF];
		}
	}
	addRoundKey(block, roundKeys[0]);
}


int main() {
	unsigned char plain[] = { 0, 0, 0, 0, 0, 0, 0, 0 };
	unsigned char key[] = { 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF };

	encrypt(plain, key);

	for (int i = 0; i < 8; i++)
	{
		printf_s("%.2x",plain[i]);
	}
	printf_s("\n");
	system("pause");
	return 0;
}