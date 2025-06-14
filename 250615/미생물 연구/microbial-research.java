import java.io.*;
import java.util.*;

public class Main {
    // 최대 배양 용기, 최대 미생물 개수
    public static final int MAX_CONTAINER = 20;
    public static final int MAX_MICRO = 55;

    // 상 좌 하 우 이동용
    public static final int[] dRow = {0, -1, 0, 1};
    public static final int[] dCol = {-1, 0, 1, 0};

    // 배양 용기 크기, 미생물 갯수 횟수
    public static int containerSize, microNumber;

    // 미생물 영역 넓이
    public static int[] microSize = new int[MAX_MICRO];

    // 기존 배양 용기
    public static int[][] microContainer = new int[MAX_CONTAINER][MAX_CONTAINER];
    // 이동용 배양 용기
    public static int[][] newMicroContainer = new int[MAX_CONTAINER][MAX_CONTAINER];

    // 미생물 분리 여부
    public static int[] seperateMicroNumber = new int[MAX_MICRO];

    // 방문 여부 확인
    public static boolean[][] check = new boolean[MAX_CONTAINER][MAX_CONTAINER];

    // 각 미생물의 최소, 최대 위치
    public static class Pair{
        public int first;
        public int second;
        public Pair(int first, int second){
            this.first = first;
            this.second = second;
        }
    }
    public static Pair[] microStartLocation = new Pair[MAX_MICRO];
    public static Pair[] microEndLocation = new Pair[MAX_MICRO];

    // 미생물 크기 순서로 정렬
    public static class Micro implements Comparable<Micro>{
        public int size;
        public int id;
        public Micro(int size, int id){
            this.size = size;
            this.id = id;
        }

        @Override
        public int compareTo(Micro o){
            int cmp = Integer.compare(o.size, this.size);
            if(cmp != 0)    return cmp;
            return Integer.compare(this.id, o.id);
        }
    }


    public static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    public static StringBuilder sb = new StringBuilder();

    // 용기 넘어갔는지 확인
    public static boolean isOutBound(int row, int col){
        return !(0 <= row && row < containerSize && 0 <= col && col < containerSize);
    }

    // 연결된 미생물들 방문 처리
    public static void dfsVistedMicro(int microId, int startRow, int startCol){
        check[startCol][startRow] = true;

        for(int dir = 0; dir < 4; dir++){
            int newRow = startRow + dRow[dir];
            int newCol = startCol + dCol[dir];

            if(isOutBound(newRow, newCol))  continue;

            if(check[newCol][newRow])   continue;

            if(microContainer[newCol][newRow] != microId)   continue;

            dfsVistedMicro(microId, newRow, newCol);
        }
    }

    // 반으로 갈라진 미생물 없애기
    public static void removeSeperateMicro(int microId){
        for(int col = 0; col < containerSize; col++){
            for(int row = 0; row < containerSize; row++){
                if(microContainer[col][row] == microId){
                    microContainer[col][row] = 0;
                }
            }
        }
    }

    // 미생물 투입 단계
    public static void insertNewMicro(int microId, int r1, int c1, int r2, int c2){
        // 체크 및 분리여부 초기화
        for(int col = 0; col < containerSize; col++){
            for(int row = 0; row < containerSize; row++){
                check[col][row] = false;
            }
        }
        for(int id = 1; id <= microId; id++){
            seperateMicroNumber[id] = 0;
        }

        // 새로운 미생물 투입 
        for(int col = c1; col < c2; col++){
            for(int row = r1; row < r2; row++){
                microContainer[col][row] = microId;
            }
        } 

        // 미생물 분리 여부 확인
        for(int col = 0; col < containerSize; col++){
            for(int row = 0; row < containerSize; row++){
                if(microContainer[col][row] == 0)      continue;
                if(check[col][row])     continue;
                seperateMicroNumber[microContainer[col][row]]++;
                // 연결된 미생물 방문 처리
                dfsVistedMicro(microContainer[col][row], row, col);
            }
        }

        // 미생물 분리 시 분리된 미생물 없애기
        for(int id = 1; id <= microId; id++){
            if(seperateMicroNumber[id] >= 2){
                removeSeperateMicro(id);
            }
        }
    }

    // 배양 용기 이동 단계
    public static void moveNewMicroContainer(int microId){
        // 새로운 배양 용기 비우기
        for(int col = 0; col < containerSize; col++){
            for(int row = 0; row < containerSize; row++){
                newMicroContainer[col][row] = 0;
            }
        }

        // 미생물 최대 최소 초기화
        for (int id = 1; id <= microId; id++) {
            microSize[id] = 0;
            // 미생물 시작 좌표는 매우 큰 값으로 초기화
            microStartLocation[id] = new Pair(Integer.MAX_VALUE, Integer.MAX_VALUE);
            // 미생물 종료 좌표는 0으로 초기화
            microEndLocation[id] = new Pair(0, 0);
        }

        // 각 미생물의 최대, 최소 구하기
        for(int col = 0; col < containerSize; col++){
            for(int row = 0; row < containerSize; row++){
                int id = microContainer[col][row];
                if(id == 0)     continue;

                microSize[id]++;

                microStartLocation[id].first = Math.min(row ,microStartLocation[id].first);
                microStartLocation[id].second = Math.min(col ,microStartLocation[id].second);
                microEndLocation[id].first = Math.max(row ,microEndLocation[id].first);
                microEndLocation[id].second = Math.max(col ,microEndLocation[id].second);
            }
        }


        // 크기에 따라 미생물 정렬
        ArrayList<Micro> microOrder = new ArrayList<>();
        for(int id = 1; id <= microId; id++){
            //System.out.println("순서 : " + id + ", 사이즈 : " + microSize[id]);
            if(microSize[id] == 0)      continue;

            microOrder.add(new Micro(microSize[id], id));
        }
        Collections.sort(microOrder);

        // 순서대로 새로운 배양 용기에 집어넣기
        for(Micro order : microOrder){
            int id = order.id;
            // 미생물의 최대 가로, 세로 길이 
            Pair boundingStart = microStartLocation[id];
            Pair boundingEnd = microEndLocation[id];

            int possibleRowSize = boundingEnd.first - boundingStart.first + 1;
            int possibleColSize = boundingEnd.second - boundingStart.second + 1;
            for(int row = 0; row <= containerSize - possibleRowSize; row++){
                boolean placedForThisRow  = false;
                for(int col = 0; col <= containerSize - possibleColSize; col++){
                    boolean isPossible = true;
                    // 집어넣을 수 있는 위치 파악
                    for(int checkCol = 0; checkCol < possibleColSize; checkCol++){
                        for(int checkRow = 0; checkRow < possibleRowSize; checkRow++){
                            int originRow = microStartLocation[id].first + checkRow;
                            int originCol = microStartLocation[id].second + checkCol;

                            if(microContainer[originCol][originRow] != id)  continue;

                            if(newMicroContainer[col+checkCol][row+checkRow] != 0){
                                isPossible = false;
                                break;
                            }
                        }
                        if(!isPossible){
                            break;
                        }
                    }

                    // 만약 가능하면 새 배양 용기에 집어넣기
                    if(isPossible){
                        for(int checkCol = 0; checkCol < possibleColSize; checkCol++){
                            for(int checkRow = 0; checkRow < possibleRowSize; checkRow++){
                                int originRow = microStartLocation[id].first + checkRow;
                                int originCol = microStartLocation[id].second + checkCol;

                                if(microContainer[originCol][originRow] != id)      continue;

                                newMicroContainer[col+checkCol][row+checkRow] = id;
                            }
                        }
                        placedForThisRow = true;
                        break;
                    }
                }
                if(placedForThisRow){
                    break;
                }
            }
        }

        // 새로운 배양 용기 -> 기존 용기로 이동
        for(int col = 0; col < containerSize; col++){
            for(int row = 0; row < containerSize; row++){
                microContainer[col][row] = newMicroContainer[col][row];
            }
        }


    }

    // 실험 결과 기록 단계
    public static void writeExperimentHistory(int microId){
        boolean[][] isAdjacent = new boolean[MAX_MICRO][MAX_MICRO];
    
        // 배양 용기 전체를 순회하면서 각 셀의 인접 셀을 확인합니다.
        for (int col = 0; col < containerSize; col++) {
            for (int row = 0; row < containerSize; row++) {
                // 현재 셀이 빈 셀인 경우에는 넘어갑니다.
                if (microContainer[col][row] == 0) continue;
                // 4방향 인접 셀을 확인합니다.
                for (int dir = 0; dir < 4; dir++) {
                    int adjRow = row + dRow[dir];
                    int adjCol = col + dCol[dir];
                    // 인접 셀이 배양 용기 범위를 벗어나면 넘어갑니다.
                    if (isOutBound(adjRow, adjCol)) continue;
                    // 인접 셀이 빈 셀인 경우에는 넘어갑니다.
                    if (microContainer[adjCol][adjRow] == 0) continue;
                    // 현재 셀과 인접 셀의 미생물 무리 id가 서로 다르면 인접 관계를 기록합니다.
                    if (microContainer[col][row] != microContainer[adjCol][adjRow]) {
                        int idA = microContainer[col][row];
                        int idB = microContainer[adjCol][adjRow];
                        isAdjacent[idA][idB] = true;
                        isAdjacent[idB][idA] = true;
                    }
                }
            }
        }
    
        int experimentScore = 0;
        // 모든 미생물 무리 쌍에 대해 인접한 경우 d각 무리의 영역 넓이 곱을 실험 점수에 더합니다.
        // 각 미생물 무리 id 쌍은 한 번씩만 계산하도록 (idA < idB) 조건을 사용합니다.
        for (int idA = 1; idA <= microId; idA++) {
            for (int idB = idA + 1; idB <= microId; idB++) {
                if (isAdjacent[idA][idB]) {
                    experimentScore += microSize[idA] * microSize[idB];
                }
            }
        }
        // 계산된 실험 점수를 출력합니다.
        sb.append(experimentScore).append("\n");
    }

    public static void main(String[] args) throws IOException{
        StringTokenizer st = new StringTokenizer(br.readLine().trim());

        containerSize = Integer.parseInt(st.nextToken());
        microNumber = Integer.parseInt(st.nextToken());

        for(int micro = 1; micro <= microNumber; micro++){
            st = new StringTokenizer(br.readLine().trim());

            int r1 = Integer.parseInt(st.nextToken());
            int c1 = Integer.parseInt(st.nextToken());
            int r2 = Integer.parseInt(st.nextToken());
            int c2 = Integer.parseInt(st.nextToken());

            // 미생물 투입 단계 
            insertNewMicro(micro, r1, c1, r2, c2);
            // 배양 용기 이동 단계
            moveNewMicroContainer(micro);
            // 실험 결과 기록 단계 
            writeExperimentHistory(micro);
        }

        System.out.println(sb);


    }
}