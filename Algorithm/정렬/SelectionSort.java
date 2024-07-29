package sort;

/**
 * 선택 정렬
 * <br/>
 * 특징:
 * 추가적 공간을 소모하지 않아 효율적이며, 최선과 최악 모두 N(N - 1) / 2 번 탐색을 진행하기 때문에 일관적인 속도를 가진다.
 * 버블 정렬에 비해서는 교환 연산이 작기 때문에 좀 더 빠른 성능을 보이고, 삽입 정렬에 비해서는 비교 연산이 많아 성능이 나쁘다.
 * 또한, 불안정 정렬이라는 단점을 가진다.
 * <br/>
 * 시간복잡도: N(N - 1) / 2
 * 최선: O(N^2)
 * 최악: O(N^2)
 * <br/>
 * 공간복잡도: O(N)
 * type: 불안정 정렬
 */
public class SelectionSort {
    public static void sort(Student[] arr) {
        for (int i = 0; i < arr.length - 1; i++) {
            int target = i;

            for (int j = 1; j < arr.length - i; j++) {
                if (arr[j - 1].score > arr[j].score) {
                    target = j;
                }
            }
            swap(i, target, arr);
        }
    }

    private static void swap(int a, int b, Student[] arr) {
        Student tmp = arr[a];
        arr[a] = arr[b];
        arr[b] = tmp;
    }
}
