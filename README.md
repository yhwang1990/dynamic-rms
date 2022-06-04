# Dynamic-RMS

A Java implementation of the FD-RMS algorithm in our paper "A Fully Dynamic Algorithm for k-Regret Minimizing Sets", which is available on <https://arxiv.org/abs/2005.14493>. All baseline algorithms we evaluate in our experiments are also available online. Please refer to <https://users.cs.duke.edu/~ssintos/kRMS_SEA/> for the implementation of *eps-Kernel*, *Greedy*, and *HittingSet*; and <https://www.cse.ust.hk/~raywong/code/sigmod18-sphere.zip> for the implementation of *DMM-Greedy*, *DMM-RRMS*, *GeoGreedy*, and *Sphere*.

## Requirements

JDK 8+

## Dependencies

- Apache Commons CLI 1.4, downloaded from <https://mvnrepository.com/artifact/commons-cli/commons-cli/1.4>  

## Usage

### 1. Input Format

**Data File**: The first line of the data file specifies the dimensionality of the dataset; From the second line of the data file, each line represents one tuple in the dataset with different attributes split by a single space. Please refer to [dataset/NBA/NBA.txt](dataset/NBA/NBA.txt) for an example.

**Workload File**: Each line of the workload file represents one tuple deletion from the dataset. Please refer to [dataset/NBA/NBA_wl.txt](dataset/NBA/NBA_wl.txt) for an example.

If you need other datasets used in our paper, feel free to contact [Yanhao Wang](mailto:yhwang@dase.ecnu.edu.cn). We do not include them in this repository because their sizes are too large.

### 2. How to Run the Code

To run FD-RMS with the default parameter settings:

```shell
    $ runRMS.jar -d <dim> -f <dataset> -k <k> -r <r> -s <size>
    <dim>: Integer, the dimensionality of the dataset
    <dataset>: String, the name of the dataset, ["BB", "AQ", "CT", "Movie", "Indep", "AntiCor"]
    <k>: Integer, the value of k in k-RMS
    <r>: Integer, the size constraint of k-RMS
    <size>: Integer, the size of the dataset, for Indep and AntiCor only
```

To run FD-RMS with varying the value of epsilon:

```shell
    $ runRMS-eps.jar -d <dim> -f <dataset> -k <k> -r <r> -s <size>
    <dim>: Integer, the dimensionality of the dataset
    <dataset>: String, the name of the dataset, ["BB", "AQ", "CT", "Movie", "Indep", "AntiCor"]
    <k>: Integer, the value of k in k-RMS
    <r>: Integer, the size constraint of k-RMS
    <size>: Integer, the size of the dataset, for Indep and AntiCor only
```

To run FD-RMS with customized parameter settings:

```shell
    $ runRMS-inst.jar  -d <dim> -e <epsilon> -i <input> -k <k> -m <M> -o <output> -r <r>
    <dim>: Integer, the dimensionality of the dataset
    <epsilon>: Float between 0 and 1, the value of epsilon in FD-RMS
    <input>: String, the path of the data file
    <k>: Integer, the value of k in k-RMS
    <M>: Integer between 10 and 20 (M is set to 2^10 to 2^20 accordingly), the value of M in FD-RMS
    <output>: String, the path of the result files
    <r>: Integer, the size constraint of k-RMS
```

## Contact

If there is any question, feel free to contact [Yanhao Wang](mailto:yhwang@dase.ecnu.edu.cn).
