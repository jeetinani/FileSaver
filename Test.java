public class Test {
    public static void main(String[] args) {
        int[] arr = { -4 }; // -4
        System.out.println("ans is " + find(arr));
        int[] arr1 = { 0 }; // 0
        System.out.println("ans is " + find(arr1));
        int[] arr2 = { 0, 0, 0, 0, -4 }; // 0
        System.out.println("ans is " + find(arr2));
        int[] arr3 = { 0, 0, 0, 0, 4, 7, 6 }; // 168
        System.out.println("ans is " + find(arr3));
        int[] arr4 = { 0, 0, 0, 0, -6, -7, -4 }; // 42
        System.out.println("ans is " + find(arr4));
        int[] arr5 = { 0, 0, 0, 0, -4, 7, 6 }; // 42
        System.out.println("ans is " + find(arr5));
        int[] arr6 = { 0, -4, 1 }; // 1
        System.out.println("ans is " + find(arr6));
    }

    public static long find(int[] outputLevels) {
        Long answer = null;
        int highestNegative = Integer.MIN_VALUE;
        Integer zeroOrUnitValue = null;
        for (int i = 0; i < outputLevels.length; i++) {
            if (outputLevels[i] == 0 || outputLevels[i] == 1) {
                if (zeroOrUnitValue == null || outputLevels[i] > zeroOrUnitValue) {
                    zeroOrUnitValue = outputLevels[i];
                }
                continue;
            }
            if (outputLevels[i] < 0 && outputLevels[i] > highestNegative) {
                highestNegative = outputLevels[i];
            }
            if (answer == null) {
                answer = (long) outputLevels[i];
            } else {
                answer *= outputLevels[i];
            }
        }
        if (answer == null) {
            return 0;
        }
        if (answer == highestNegative) {
            if (zeroOrUnitValue != null) {
                return zeroOrUnitValue;
            }
            return answer;
        }
        if (answer < 0) {
            return (answer / highestNegative);
        }
        return answer;
    }

    /*
     * int[] testCase1 = { 0 };
     * int[] testCase2 = { -4 };
     * int[] testCase3 = { 0, 0, 0, 0, -4 };
     * int[] testCase4 = { 0, 0, 0, 0, 4, 7, 6 };
     * int[] testCase5 = { 0, 0, 0, 0, -6, -7, -4 };
     * int[] testCase6 = { 0, 0, 0, 0, -4, 7, 6 };
     * int[] testCase7 = { 0, -4, 1 };
     */

    public static long findLastWorking(int[] arr) {
        long maxNegative = Integer.MIN_VALUE;
        Long ans = null;
        int pickedCount = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == 0) {
                continue;
            }
            pickedCount += 1;
            if (ans != null) {
                ans *= arr[i];
            } else {
                ans = (long) arr[i];
            }
            if (arr[i] < 0 && arr[i] > maxNegative) {
                maxNegative = arr[i];
            }
        }
        if (ans == null) {
            return 0;
        }
        if (ans < 0 && pickedCount == 1) {
            return pickedCount < arr.length ? 0L : ans;
        }
        return ans < 0 ? (ans / maxNegative) : ans;
    }
}