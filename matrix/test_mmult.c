#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>

#include "mat.h"

int test(int method, double *a, int arows, int acols,
                     double *b, int brows, int bcols,
                     double *c_actual) {
    double *c_calc = malloc(arows * arows * sizeof(double));

    switch (method)
    {
    case 1:
        mmult(c_calc, a, arows, arows, b, brows, brows);
        break;
    case 2:
        mmult_simd(c_calc, a, arows, arows, b, brows, brows);
        break;
    default:
        printf("Invalid method selected\n");
        return -1;
        break;
    }
    

    int are_same = compare_matrices(c_actual, c_calc, brows, brows);

    free(c_calc);

    return are_same;
}

int str_to_int(const char* s){
    int x;
    sscanf(s, "%d", &x);
    return x;
}

/*
Arguments are in the following order:
method, matrix size, a-file, b-file, c_actual-file
*/
int main(int argc, char** argv) {
    int method = str_to_int(argv[1]);
    int MAT_SIZE = str_to_int(argv[2]);

    double *a = read_matrix_from_file(argv[3]);
    double *b = read_matrix_from_file(argv[4]);
    double *c_actual = read_matrix_from_file(argv[5]);
    double *c_calc = malloc(MAT_SIZE * MAT_SIZE * sizeof(double));

    if(!test(method, a, MAT_SIZE, MAT_SIZE, b, MAT_SIZE, MAT_SIZE, c_actual)) {
        exit(1);
    }

    puts("All tests pass.");

    free(a);
    free(b);
    free(c_actual);
}
