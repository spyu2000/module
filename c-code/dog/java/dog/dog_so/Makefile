all: libDOGJava.so

#Which compiler
CC = gcc

#Where are include files kept
INCLUDE = /opt/oracle/product/11.2/jdk/include

#Options for development
CFLAGS = -O -Wall -ansi

libDOGJava.so: Dogimp.o mhlinuxc.o
	$(CC) -shared  -o  $@ Dogimp.o mhlinuxc.o
Dogimp.o: Dogimp.c version.c gsmh.h gsdog.h 
	$(CC) -c Dogimp.c -I$(INCLUDE) -I$(INCLUDE)/linux

Clean:
	-rm Dogimp.o  
