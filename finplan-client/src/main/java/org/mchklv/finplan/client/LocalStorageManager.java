package org.mchklv.finplan.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.mchklv.finplan.common.ProtectedArea;

import javafx.scene.image.Image;
import javafx.stage.Screen;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

public class LocalStorageManager {
    private static class CountedString implements Serializable {
		private static final long serialVersionUID = 44777767792987674L;
        
		private String string;
        private int count;

        public CountedString(String string, int count) {
            this.string = string;
            this.count = count;
        }
		public String getString() {
			return string;
		}
		public void setString(String string) {
			this.string = string;
		}
		public int getCount() {
			return count;
		}
		public void setCount(int counter) {
			this.count = counter;
		}
    }

    
    private static SettingsManager settingsManager;
    private static Map<String, Image> resoureImageCache;
    private static List<CountedString> nonStandardProgrammes;
    private static String programmesFilePath;


    public static void initLocalStorageManager() {
        settingsManager = new SettingsManager();
        resoureImageCache = new HashMap<>();
        loadProgrammesStorage();
    }
    


	public static SettingsManager getSettingsManager() {
		return settingsManager;
	}
    

    public static List<Image> getAppIcons() {
        final String[] iconsResourcePaths = {
            "images/icons/app_icon_16x16.png",
            "images/icons/app_icon_32x32.png",
            "images/icons/app_icon_48x48.png",
            "images/icons/app_icon_256x256.png" };

        ArrayList<Image> appIcons = new ArrayList<Image>(4);
        
        for (int i = 0; i < iconsResourcePaths.length; ++i) {
            String imagePath = LocalStorageManager.class.getClassLoader().getResource(iconsResourcePaths[i]).toExternalForm();
            Image image = new Image(imagePath);
            appIcons.add(image);
        }

        return appIcons;
    }

    public static Image getImageResource(String imageId, double relativeWidth, double relativeHeight) {
        String cacheImageId = String.format("%.2f", relativeWidth) + "x" +
            String.format("%.2f", relativeHeight) + "_" + imageId;
        Image cachedImage = resoureImageCache.get(cacheImageId);
        if (cachedImage != null) {
            return cachedImage;
        }

        String imagePath = LocalStorageManager.class.getClassLoader().getResource("images/" + imageId).toExternalForm();
        Image image = null;

        double requestedWidth = Screen.getPrimary().getDpi() * relativeWidth / 157.0;
        double requestedHeight = Screen.getPrimary().getDpi() * relativeHeight / 157.0;

        try {
            image = new Image(imagePath, requestedWidth, requestedHeight, false, true);
        }
        catch (IllegalArgumentException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }

        if (image == null) {
            imagePath = LocalStorageManager.class.getClassLoader().
                getResource("images/generic_image_placeholder_32x32.png").toExternalForm();
            image = new Image(imagePath);
        }
        else {
            resoureImageCache.put(cacheImageId, image);
        }

        return image;
    }



    public static String[] getPAProgrammes(boolean isAdmEcon) {
        String[] defaultProgrammes = null;
        List<String> nonStandardProgsStrs = new ArrayList<String>();
        
        if (isAdmEcon) {
            defaultProgrammes = settingsManager.getDefaultAdmEconProgrammes();
        }
        else {
            defaultProgrammes = settingsManager.getDefaultProgrammes();
        }

        for (CountedString cStr : nonStandardProgrammes) {
            if (cStr.getCount() >= 1) {
                nonStandardProgsStrs.add(cStr.getString());
            }
        }
        
        String[] resProgrammes = new String[defaultProgrammes.length + nonStandardProgsStrs.size()];
        for (int i = 0; i < nonStandardProgsStrs.size(); ++i) {
            resProgrammes[i] = nonStandardProgsStrs.get(i);
        }
        for (int i = 0; i < defaultProgrammes.length; ++i) {
            resProgrammes[nonStandardProgsStrs.size() + i] = defaultProgrammes[i];
        }

        return resProgrammes;
    }

    
    public static void paProgrammeSelected(String programme) {
        if (programme == null || programme.isEmpty()) {
            return;
        }
        
        String[] defaultProgrammes = settingsManager.getDefaultAdmEconProgrammes();
        for (int i = 0; i < defaultProgrammes.length; ++i) {
            if (defaultProgrammes[i].equals(programme)) {
                return;
            }
        }

        for (CountedString cStr : nonStandardProgrammes) {
            if (cStr.getString().equals(programme)) {
                cStr.setCount(cStr.getCount() + 1);
                flushProgrammesStorage();
                return;
            }
        }

        nonStandardProgrammes.add(new CountedString(programme, 1));
        flushProgrammesStorage();
    }

    public static void paProgrammeDeselected(String programme) {
        if (programme == null || programme.isEmpty()) {
            return;
        }
        
        String[] defaultProgrammes = settingsManager.getDefaultAdmEconProgrammes();
        for (int i = 0; i < defaultProgrammes.length; ++i) {
            if (defaultProgrammes[i].equals(programme)) {
                return;
            }
        }

        CountedString stringToDelete = null;
        for (CountedString cStr : nonStandardProgrammes) {
            if (cStr.getString().equals(programme)) {
                cStr.setCount(cStr.getCount() - 1);
                if (cStr.getCount() <= 0) {
                    stringToDelete = cStr;
                    break;
                }
            }
        }

        if (stringToDelete != null) {
            nonStandardProgrammes.remove(stringToDelete);
            flushProgrammesStorage();
        }
    }

    
    private static void loadProgrammesStorage() {
        AppDirs appDirs = AppDirsFactory.getInstance();
        String dataDirPath = appDirs.getUserDataDir("finplan", null, "mhklv");
        File dataDirFile = new File(dataDirPath);
      
        if (!dataDirFile.exists()) {
            dataDirFile.mkdirs();
        }

        programmesFilePath = dataDirPath + File.separator + "custom-progs.ser";
        File progsFile = new File(programmesFilePath);
        ObjectInputStream fin = null;

        if (progsFile.exists()) {
            try {
                fin =
                    new ObjectInputStream(
                        new BufferedInputStream(
                            new FileInputStream(progsFile)));
                Object fileObject = fin.readObject();
                nonStandardProgrammes = (List<CountedString>) fileObject;
            }
            catch (IOException | ClassNotFoundException e) {
                System.err.println("Error " + e.getMessage());
                e.printStackTrace();
            }
            finally {
                try {
                    if (fin != null) {
                        fin.close();
                    }
                    if (nonStandardProgrammes == null) {
                        nonStandardProgrammes = new ArrayList<CountedString>();
                    }
                }
                catch (IOException e) {
                    System.err.println("Error " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        else {
            try {
                progsFile.createNewFile();
                nonStandardProgrammes = new ArrayList<CountedString>();
                flushProgrammesStorage();
            }
            catch (IOException e) {
                System.err.println("Error " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void flushProgrammesStorage() {
        if (nonStandardProgrammes == null || nonStandardProgrammes.isEmpty()) {
            return;
        }

        ObjectOutputStream fout = null;

        try {
            fout =
                new ObjectOutputStream(
                    new BufferedOutputStream(
                        new FileOutputStream(programmesFilePath)));
            fout.writeObject(nonStandardProgrammes);
        }
        catch (IOException e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
        finally {
            try {
                if (fout != null) {
                    fout.close();
                }
            }
            catch (IOException e) {
                System.err.println("Error " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
