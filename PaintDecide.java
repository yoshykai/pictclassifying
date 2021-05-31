import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.imageio.ImageIO;

/*import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;*/

public class PaintDecide {
	public static void main(String[]args) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		//final String pass = "C:\\Users\\yoshy\\Pictures\\音ゲー記録\\";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd HH mm ss");
		Calendar cal = Calendar.getInstance();

		int hist[][][] = new int[6][5][]; //0:太鼓(青) 1:太鼓(赤) 2:SDVX 3:グルコス 4:チュウニ旧 5:チュウニ新
		int avehist[][] = new int[6][256];
		Sysytem.out.print("0:そのまま実行 1:データ更新してから実行 >>");
		int a = Integer.parseInt(br.readLine());
		if(a==0){
			avehist = fileRead();
		}else{
			for(int i=0;i<6;i++){
				for(int j=1;j<=5;j++){
					hist[i][j-1] = Histgram.hist("testdata/"+i+"-"+j+".JPG");
				}
				System.out.println(i+"終了");
			}
			for(int i=0;i<6;i++){
				for(int k=0;k<256;k++){
					int n = 0;
					for(int j=0;j<5;j++){
						n += hist[i][j][k];
					}
					avehist[i][k] = n/5;
				}
				System.out.println(i+"終了");
			}
			fileWrite(avehist);
		}
		System.out.print("最大値入力>>>");
		a = Integer.parseInt(br.readLine());
		ArrayList<Map<String,Integer>> map = new ArrayList<Map<String,Integer>>();
		for(int i=0;i<4;i++){
			map.add(new HashMap<String,Integer>());
		}
		for(int num=1;num<=a;num++){
			int cla = -1;
			/*String str = "";
			if(num<10){
				str = "00"+num;
			}else if(num<100){
				str = "0"+num;
			}else{
				str = ""+num;
			}*/
			BufferedImage bi = ImageIO.read(new File("data/"+num+".jpeg"));
			String lastModifiedStr = sdf.format(file.lastModified()).substring(0,10);
			int time[] = stoi(lastModifiedStr.split(" "));
			//cal.clear();
			//cal.set(time[0],time[1]-1,time[2],time[3],time[4],time[5]);
			int test[] = Histgram.hist(bi);
			long di = Long.MAX_VALUE;
			for(int i=0;i<6;i++){
				long s = dist(test,avehist[i]);
				if(di>s){
					di = s;
					cla = i;
				}
			}
			String pass = "";
			int number = 0;
			switch(cla){
			case 0:
			case 1:
				pass = "taiko/";
				number = getNum(map.get(0),lastModifiedStr);
				break;
			case 2:
				pass = "sdvx/";
				number = getNum(map.get(1),lastModifiedStr);
				break;
			case 4:
			case 5:
				pass = "tyuni/";
				number = getNum(map.get(2),lastModifiedStr);
				break;
			case 3:
				pass = "gurukosu/";
				number = getNum(map.get(3),lastModifiedStr);
				break;
			}
			ImageIO.write(bi, "JPG",new File(pass+time[0]+"-"+time[1]+"-"+time[2]+"-"+number+".JPG"));
			//f.setLastModified(cal.getTimeInMillis()); //更新日を変更
			System.out.println(num+"終了");
		}

	}

	public static int getNum(Map<String,Integer> m,String str){
		if(m.get(str)==null){
			m.put(str,1);
			return 1;
		}
		m.replace(str,m.get(str)+1);
		return m.get(str);
	}

	public static int[] stoi(String s[]){
    int a[]=new int[s.length];
    for(int i=0;i<s.length;i++){
      a[i]=stoi(s[i]);
    }
    return a;
  }

	private static long dist(int[]a,int[]b){
		long n=0;
		for(int i=0;i<a.length;i++){
			n += Math.abs(a[i]-b[i]);
		}
		return n;
	}

	private static int[][] fileRead() throws IOException{
		BufferedReader brf = new BufferedReader(new FileReader("pict.txt"));
		int re[][] = new int[6][256];
		String str = "";
		int i=0;
		while((str=brf.readLine())!=null){
			String w[] = str.split(" ");
			for(int j=0;j<256;j++){
				re[i][j] = Integer.parseInt(w[j]);
			}
			i++;
		}

		brf.close();

		return re;
	}

	private static void fileWrite(int[][] a) throws IOException{
		BufferedWriter bwf = new BufferedWriter(new FileWriter("pict.txt"));
		for(int i=0;i<a.length;i++){
			for(int j=0;j<a[i].length;j++){
				bwf.write(String.valueOf(a[i][j]));
				if(j!=a[i].length-1){
					bwf.write(" ");
				}
			}
			bwf.newLine();
		}

		bwf.close();
	}

	/*public static int getOrientation(File in)
			throws IOException, MetadataException, ImageProcessingException {
		try {
			Metadata metadata = ImageMetadataReader.readMetadata(in);
			Directory directory = metadata.getDirectory(ExifIFD0Directory.class);
			//JpegDirectory jpegDirectory = metadata.getDirectory(JpegDirectory.class);

			int orientation = 1;
			try {
				orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
			} catch (MetadataException me) {

			}

			return orientation;
		} catch (Exception e) {

		}
		return 0;
	}*/
}

class Histgram{
	public static int[] hist(String name){
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(new File(name));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		return hist(bi);
	}

	public static int[] hist(BufferedImage bi){
		int[] histgram;
		histgram = new int[256];
		for(int j=0;j<bi.getHeight();j++){
			for(int i=0;i<bi.getWidth();i++){
				int color = gray(bi.getRGB(i,j));
				histgram[color]++;
			}
		}
		return histgram;
	}

	private static int red(int c){
		return (c>>16) & 0xff;
	}

	private static int green(int c){
		return (c>>8) & 0xff;
	}

	private static int blue(int c){
		return c & 0xff;
	}

	private static int gray(int c){
		int r = red(c);
		int g = green(c);
		int b = blue(c);
		return gray(r,g,b);
	}

	private static int gray(int r,int g,int b){
		int p = (int)(0.299*r+0.587*g+0.114*b);
		return p;
	}
}
