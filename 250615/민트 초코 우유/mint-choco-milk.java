import java.io.*;
import java.util.*;

public class Main{

    // 입출력
    public static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    public static StringBuilder sb = new StringBuilder();
    public static StringTokenizer st;

    // 최대 책상 크키, 최대 날짜
    public static final int MAX_DESK = 55;
    public static final int MAX_DAY = 35;

    // 상 하 좌 우
    public static final int[] drow = {-1, 1, 0, 0};
    public static final int[] dcol = {0, 0, -1, 1};

    // 책상 크기, 날짜
    public static int deskSize, dayCount;

    // 각 학생의 초기 신봉 음식
    public static int[][] believeFood = new int[MAX_DESK][MAX_DESK];

    // 각 학생의 초기 신앙심
    public static int[][] believeCount = new int[MAX_DESK][MAX_DESK];

    // 각 학생 방문 체크
    public static boolean[][] visited = new boolean[MAX_DESK][MAX_DESK];

    // 그룹 ArrayList 생성
    public static ArrayList<Group> groupOrder = new ArrayList<>();

    // 그룹
    public static class Group implements Comparable<Group>{
        int food;    // 섬기는 음식
        int row;
        int col;
        int count;

        public Group(int food, int row, int col, int count){
            this.food = food;
            this.row = row;
            this.col = col;
            this.count = count;
        }

        @Override
        public int compareTo(Group g){
            int cmp = Integer.compare(this.food, g.food);
            if(cmp != 0) return cmp;

            int cmb = Integer.compare(g.count, this.count);
            if(cmb != 0) return cmb;

            int cmr = Integer.compare(this.row, g.row);
            if(cmr != 0) return cmr;

            return Integer.compare(this.col, g.col);
        }
    }

    public static boolean isOutBounds(int row, int col){
        return !(row >= 0 && col >= 0 && row < deskSize && col < deskSize);
    }

    // 변경된 인접한 그룹 형성
    public static void createGroup(int startRow, int startCol){
        List<int[]> group = new ArrayList<>();
        Queue<int[]> queue = new LinkedList<>();
        int foodType = believeFood[startRow][startCol];

        visited[startRow][startCol] = true;
        queue.add(new int[]{startRow, startCol});
        group.add(new int[]{startRow, startCol});

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int row = cur[0];
            int col = cur[1];

            for (int dir = 0; dir < 4; dir++) {
                int newRow = row + drow[dir];
                int newCol = col + dcol[dir];
                if (isOutBounds(newRow, newCol)) continue;
                if (visited[newRow][newCol]) continue;
                if (believeFood[newRow][newCol] != foodType) continue;

                visited[newRow][newCol] = true;
                queue.add(new int[]{newRow, newCol});
                group.add(new int[]{newRow, newCol});
            }
        }

        int leaderRow = deskSize, leaderCol = deskSize, leaderBelieve = -1;
        for (int[] pos : group) {
            int row = pos[0], col = pos[1];
            int b = believeCount[row][col];
            if (b > leaderBelieve || (b == leaderBelieve && row < leaderRow)
                    || (b == leaderBelieve && row == leaderRow && col < leaderCol)) {
                leaderRow = row;
                leaderCol = col;
                leaderBelieve = b;
            }
        }

        for (int[] pos : group) {
            believeCount[pos[0]][pos[1]]--;
        }
        believeCount[leaderRow][leaderCol] += group.size();

        int type = believeFood[leaderRow][leaderCol];
        groupOrder.add(new Group(type == 0 ? 3 : (type < 4 ? 2 : 1), leaderRow, leaderCol, believeCount[leaderRow][leaderCol]));
    }

    public static int transferCase(int startTrans, int endTrans){
        int bigger = startTrans > endTrans ? startTrans : endTrans;
        int smaller = startTrans > endTrans ? endTrans : startTrans;

        if(smaller == 0) return 0;
        if(smaller == 1){
            if(bigger == 2 || bigger == 3 || bigger == 4) return 0;
            return smaller;
        }
        if(smaller == 2){
            if(bigger == 3 || bigger == 5) return 0;
            return smaller;
        }
        if(smaller == 3){
            if(bigger == 6) return 0;
            return smaller;
        }
        if(smaller == 4){
            if(bigger == 5) return 3;
            return 2;
        }
        return 1;
    }

    public static void startTransfer(int direct, int row, int col, int food, int please){
        int newRow = row + drow[direct];
        int newCol = col + dcol[direct];
        int count = please;

        if(isOutBounds(newRow, newCol)) return;
        if(food != believeFood[newRow][newCol]){
            if(please > believeCount[newRow][newCol]){
                believeFood[newRow][newCol] = food;
                count -= (believeCount[newRow][newCol] + 1);
                believeCount[newRow][newCol]++;

                visited[newRow][newCol] = true;
                if(count == 0) return;
            }else{
                believeCount[newRow][newCol] += count;
                believeFood[newRow][newCol] = transferCase(food, believeFood[newRow][newCol]);
                visited[newRow][newCol] = true;
                return;
            }
        }
        startTransfer(direct, newRow, newCol, food, count);
    }

    public static void timeToMorning(){
        for(int row = 0; row < deskSize; row++){
            for(int col = 0; col < deskSize; col++){
                believeCount[row][col]++;
            }
        }
    }

    public static void timeToLunch(){
        for(int row = 0; row < deskSize; row++){
            for(int col = 0; col < deskSize; col++){
                visited[row][col] = false;
            }
        }
        groupOrder.clear();

        for(int row = 0; row < deskSize; row++){
            for(int col = 0; col < deskSize; col++){
                if(visited[row][col]) continue;
                createGroup(row, col);
            }
        }
        Collections.sort(groupOrder);
    }

    public static void timeToNight(int check){
        for(int row = 0; row < deskSize; row++){
            for(int col = 0; col < deskSize; col++){
                visited[row][col] = false;
            }
        }

        for(Group transfer : groupOrder){
            int please = transfer.count - 1;
            int transdirect = transfer.count % 4;

            if(visited[transfer.row][transfer.col]){
                continue;
            }

            believeCount[transfer.row][transfer.col] = 1;
            startTransfer(transdirect, transfer.row, transfer.col, believeFood[transfer.row][transfer.col], please);

        }

        int[] result = new int[7];
        for(int row = 0; row < deskSize; row++){
            for(int col = 0; col < deskSize; col++){
                result[believeFood[row][col]] += believeCount[row][col];
            }
        }

        for(int i = 0; i < 7; i++){
            sb.append(result[i]).append(" ");
        }
        sb.append("\n");
    }

    public static void main(String[] args) throws IOException{
        st = new StringTokenizer(br.readLine().trim());
        deskSize = Integer.parseInt(st.nextToken());
        dayCount = Integer.parseInt(st.nextToken());

        for(int row = 0; row < deskSize; row++){
            String foodstr = br.readLine().trim();
            for(int col = 0; col < deskSize; col++){
                switch(foodstr.charAt(col)){
                    case 'M': believeFood[row][col] = 4; break;
                    case 'C': believeFood[row][col] = 5; break;
                    case 'T': believeFood[row][col] = 6; break;
                    default:
                }
            }
        }

        for(int row = 0; row < deskSize; row++){
            st = new StringTokenizer(br.readLine().trim());
            for(int col = 0; col < deskSize; col++){
                believeCount[row][col] = Integer.parseInt(st.nextToken());
            }
        }

        for(int day = 1; day <= dayCount; day++){
            timeToMorning();
            timeToLunch();
            timeToNight(day);
        }

        System.out.println(sb);
    }
}
