package sort;

/**
 * 시간복잡도:
 * <br/>
 * 특징:
 * 추가적 공간을 소모하지 않아 효율적이다.
 * 정렬된 상태의 경우 O(N)에 끝낼 수 있어 삽입 정렬과 같이 효율적이지만, 그 이외의 상황에서는 요소들의 교환 연산이 많아
 * 다른 O(N^2)의 시간복잡도를 가지는 정렬 알고리즘에 비해 시간이 더 오래 걸린다.
 * <br/>
 * 최선: O(N) [이미 정렬 된 상태에서는 isSwapped 로 정렬 중단]
 * 최악: O(N^2) == N(N - 1) / 2
 * <br/>
 * 공간복잡도: O(N)
 * type: 안정 정렬
 */
class BubbleSort {
    public static void sort(Student[] arr) {
        boolean isSwapped = false;

        for (int i = 0; i < arr.length - 1; i++) {
            for (int j = 1; j < arr.length - i; j++) {
                if (arr[j - 1].score > arr[j].score) {
                    swap(j - 1, j, arr);
                    isSwapped = true;
                }
            }

            if (!isSwapped) {
                break;
            }
        }
    }

    private static void swap(int a, int b, Student[] arr) {
        Student tmp = arr[a];
        arr[a] = arr[b];
        arr[b] = tmp;
    }
}
