package sort;

/**
 * 삽입 정렬
 * <p>
 * 특징:
 * 추가적 공간을 소모하지 않아 효율적이며, 앞선 요소가 정렬되었을 경우 비교를 수행하지 않기 때문에 비교 횟수에 대한 기대값이 적다.
 * 때문에, 버블, 선택 정렬과 같은 시간복잡도를 가지지만, 그 중에서는 빠른 정렬을 보인다.
 * <p>
 * 시간복잡도:
 * 최선: O(N) [정렬된 상태의 경우 삽입 위치 탐색이 없음]
 * 최악: O(N^2) == N(N - 1) / 2
 * <p>
 * 공간복잡도: O(N)
 * type: 안정 정렬
 */
public class InsertionSort {
    public static void sort(Student[] arr) {
        for (int i = 1; i < arr.length - 1; i++) {
            Student current = arr[i];
            int j = i - 1;

            //요소들을 한칸 뒤로 이동
            while (j >= 0 && current.score < arr[j].score) {
                arr[j + 1] = arr[j];
                j--;
            }

            arr[j + 1] = current;
        }
    }
}
