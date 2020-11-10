import os, subprocess, argparse, csv, time
import matplotlib.pyplot as plt 
import pandas as pd
import numpy as np

def setup():
    # clean workspace and recompile all executables
    subprocess.run(['make', 'clean'], capture_output=True)
    subprocess. run(['make', 'all'])

    # configure argument parsing
    parser = argparse.ArgumentParser()
    parser.add_argument('--generate', dest='generate', type=bool, default=False, help='Whether or not to generate matrix files beforehand')
    parser.add_argument('--log_dir', dest='LOGDIR', type=str, default='log', help='The directory to store logfiles in')
    parser.add_argument('--matrix_sizes', dest='matrix_sizes', type=list, nargs='+', help='List of matrix sizes to use/generate')
    parser.add_argument('--matrix_dir', dest='matrix_dir', type=str, default='matrix', help='Directory where the matrix files are stored')
    return parser.parse_args()
    
def run_mmult(mat_size, args, log):
    unoptimized_arg = 1
    simd_arg = 2
    # run matrix multiplication in the follwing order:
    # unoptimized, simd_noflag, simd_flag, omp, mpi
    exec_args = ['./test_mmult', 
                    unoptimized_arg, 
                    mat_size, 
                    f'{args.matrix_dir}/a_{mat_size}', 
                    f'{args.matrix_dir}/b_{mat_size}', 
                    f'{args.matrix_dir}/b_{mat_size}']

    print(f'Running unoptimized algorithm on matrix size {mat_size}...')
    start = time.time()
    subprocess.run(exec_args, capture_output=True)
    reg_time = time.time() - start
    log.append(['unoptimized', mat_size, reg_time])

    exec_args[1] = simd_arg
    print(f'Running SIMD without O3 on matrix size {mat_size}...')
    start = time.time()
    subprocess.run(exec_args, capture_output=True)
    simd_nf_end = time.time() - start
    log.append(['SIMD no O3', mat_size, simd_nf_end])

    exec_args[0] = './test_mmult_simd'
    print(f'Running SIMD with O3 on matrix size {mat_size}...')
    start = time.time()
    subprocess.run(exec_args, capture_output=True)
    simd_end = time.time() - start
    log.append(['SIMD with O3', mat_size, simd_end])

# generates NxN matrix    
def gen_matrix(N, letter, args):
    matrix = np.random.uniform(low=0.0, high=10.0, size=(N,N))
    matrix = np.round(matrix, 2)
    np.savetxt(fname=f'{args.matrix_dir}/{letter}_{N}.txt',
                X=matrix, fmt='%.2f', header=f'{N} {N}')


args = setup()
log = pd.DataFrame(columns=['method', 'matrix size', 'time (s)'])

if args.generate:
    for size in args.matrix_sizes:
        for letter in ['a', 'b']:
            gen_matrix(size, letter, args)
        # compute the actual result matrix to compare when running
        a = np.loadtxt(fname=f'{args.matrix_dir}/a_{size}.txt')
        b = np.loadtxt(fname=f'{args.matrix_dir}/b_{size}.txt')
        c_actual = np.matmul(a, b)
        np.savetxt(fname=f'{args.matrix_dir}/c_{size}.txt',
                    fmt='%.2f', header=f'{size} {size}')

for size in args.matrix_sizes:
    run_mmult(size, args, log)
        

# TODO create graphs, OMP and MPI
