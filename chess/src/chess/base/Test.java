package chess.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import chess.util.Condition;


public class Test {
	public static void main(String[] args) {
		ObjectOutputStream oos = null;
		 
		try {
			File file = new File("userpieces/ser.txt");
			file.createNewFile();
			FileWriter fw = new FileWriter(file, false);
			fw.flush();
			fw.close();
			FileOutputStream fos = new FileOutputStream(file); 
			oos = new ObjectOutputStream(fos);
			oos.writeObject(Knight.getData().getTree());
			oos.flush();
			oos.close();
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
			Object obj = ois.readObject();
			System.out.println(obj);
			
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
