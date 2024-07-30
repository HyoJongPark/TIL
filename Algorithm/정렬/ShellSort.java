package sort;

/**
 * 셸 정렬
 * <br/>
 * 특징:
 * 삽입 정렬의 경우 역정렬된 배열에서 최악인 O(N^2)의 시간복잡도를 가진다. 이를 개선하고 장점을 살리는 정렬 기법이 셸 정렬이다.
 * 간격을 설정해 요소의 대략적 위치를 찾아가는 방식으로 동작한다. 간격이 너무 작으면 요소간 이동이 적어 삽입 정렬과 다를바없고,
 * 너무 크다면 각 단계에서 비교하고 교환해야 하는 요소들이 멀리 떨어져 있어서 의미 없는 비교 및 교환이 증가할 수 있다.
 * <br/>
 * 추가적 공간을 소모하지 않아 효율적이며, 시간복잡도가 gap seq에 의존적이다.
 * <br/>
 * 시간복잡도:
 * 최선: O(NlogN) [현재 리스트 크기를 2^n으로 나눌 수 있는 Gap Seq, 이전 데이터를 한 번만 비교한 후 비교하지 않아 가장 최적이다.]
 * 최악: O(N^2) [GAP == 1, 역정렬된 배열일 경우 -> 즉, 삽입 정렬]
 * <br/>
 * 공간복잡도: O(N)
 * type: 불안정 정렬
 */
public class ShellSort {

    private static final int[] GAP = {1, 4, 9, 20, 45, 102, 230, 516, 1158, 2599, 5831, 13082, 29351,
            65853, 147748, 331490, 743735, 1668650, 3743800, 8399623, 18845471, 42281871, 94863989,
            212837706, 477524607, 1071378536};

    public static void sort(Student[] arr) {
        int gapIndex = getGap(arr.length);

        for (int i = gapIndex; i >= 0; i--) {
            for (int j = 0; j < GAP[i]; j++) {
                InsertionSort.sort(arr, j, GAP[i]);
            }
        }
    }

    /**
     * 최소 부분 배열의 원소가 2개씩은 비교되도록 gap 추출
     */
    private static int getGap(int size) {
        int index = 0;
        int len = (int) (size / 2.25);

        while (GAP[index] < len) {
            index++;
        }
        return index;
    }
}
