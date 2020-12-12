package org.mchklv.finplan.client;

import javafx.geometry.Rectangle2D;
import javafx.scene.text.Font;
import javafx.stage.Screen;

public class SettingsManager {
    private Font primaryFont;
    
    private String serverAddress;
    private int remoteServerPort = 33335;

    private Rectangle2D mainWindowPos;

    private final String[] defaultAdmEconProgrammes = new String[]{
        "Административно-хозяйственное обеспечение",
        "Обеспечение охраны и предотвращение незаконных видов деятельности на территории ООПТ",
        "Обеспечение противопожарной безопасности на территории ООПТ",
        "Охрана, восстановление и разведение лесов",
        "Охрана животного мира",
        "Управление видами",
        "Управление популяциями",
        "Управление экосистемами и местообитаниями",
        "Проведение НИР",
        "Реинтродукции видов",
        "Проведение системного мониторинга в ООПТ",
        "Хозяйственная деятельность",
        "Информирование и вовлечение заинтересованных сторон",
        "Развитие экологического туризма и рекреации"
    };
    private final String[] defaultProgrammes = new String[]{
        "Обеспечение охраны и предотвращение незаконных видов деятельности на территории ООПТ",
        "Обеспечение противопожарной безопасности на территории ООПТ",
        "Охрана, восстановление и разведение лесов",
        "Охрана животного мира",
        "Управление видами",
        "Управление популяциями",
        "Управление экосистемами и местообитаниями",
        "Проведение НИР",
        "Реинтродукции видов",
        "Проведение системного мониторинга в ООПТ",
        "Хозяйственная деятельность",
        "Информирование и вовлечение заинтересованных сторон",
        "Развитие экологического туризма и рекреации"
    };

    
    
    public SettingsManager() {
        primaryFont = Font.font("Sans", 10);
        serverAddress = "api.biofinplan.ru";

        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        double screenHeight = Screen.getPrimary().getBounds().getHeight();
        setMainWindowPos(new Rectangle2D(0.125 * screenWidth, 0.085 * screenHeight, 0.75 * screenWidth,
                0.83 * screenHeight));
    }


	public String[] getDefaultAdmEconProgrammes() {
		return defaultAdmEconProgrammes;
	}
	
	public String[] getDefaultProgrammes() {
		return defaultProgrammes;
	}
	
	public Rectangle2D getMainWindowPos() {
		return mainWindowPos;
	}


	public void setMainWindowPos(Rectangle2D mainWindowPos) {
		this.mainWindowPos = mainWindowPos;
	}


	public int getRemoteServerPort() {
		return remoteServerPort;
	}

	public Font getPrimaryFont() {
		return primaryFont;
	}

	public String getServerAddress() {
		return serverAddress;
	}


	public void setPrimaryFont(Font primaryFont) {
		this.primaryFont = primaryFont;
	}


	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}


	public void setRemoteServerPort(int remoteServerPort) {
		this.remoteServerPort = remoteServerPort;
	}
}
