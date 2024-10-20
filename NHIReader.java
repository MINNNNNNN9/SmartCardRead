import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import javax.smartcardio.*;

public class NHIReader {

    // APDU 指令定義
    private static final byte[] SELECT_APDU = new byte[]{
        (byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, (byte) 0x10,
        (byte) 0xD1, (byte) 0x58, (byte) 0x00, (byte) 0x00,
        (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x11, (byte) 0x00
    };

    private static final byte[] READ_PROFILE_APDU = new byte[]{
        (byte) 0x00, (byte) 0xCA, (byte) 0x11,
        (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00
    };

    public NHIData readNHIData() throws CardException, UnsupportedEncodingException {
        TerminalFactory terminalFactory = TerminalFactory.getDefault();
        List<CardTerminal> terminals = terminalFactory.terminals().list();

        for (CardTerminal terminal : terminals) {
            if (terminal.isCardPresent()) {
                // 連接卡片
                Card card = terminal.connect("T=1");
                CardChannel channel = card.getBasicChannel();

                // 選擇應用程式
                CommandAPDU selectCommand = new CommandAPDU(SELECT_APDU);
                ResponseAPDU selectResponse = channel.transmit(selectCommand);

                // 檢查選擇應用程式是否成功
                if (selectResponse.getSW() != 0x9000) {
                    throw new CardException("選擇應用程式失敗。");
                }

                // 讀取個人資料
                CommandAPDU readCommand = new CommandAPDU(READ_PROFILE_APDU);
                ResponseAPDU readResponse = channel.transmit(readCommand);

                // 檢查讀取是否成功
                if (readResponse.getSW() != 0x9000) {
                    throw new CardException("讀取個人資料失敗。");
                }

                byte[] data = readResponse.getData();

                // 解析資料
                String cardNumber = new String(Arrays.copyOfRange(data, 0, 12));
                String name = new String(Arrays.copyOfRange(data, 12, 32), "Big5").trim();
                String idNumber = new String(Arrays.copyOfRange(data, 32, 42));
                String birthDate = new String(Arrays.copyOfRange(data, 42, 49));
                String gender = new String(Arrays.copyOfRange(data, 49, 50));


                // 建立資料對象
                NHIData nhiData = new NHIData(cardNumber, name, idNumber, birthDate, gender);

                // 斷開連接
                card.disconnect(false);

                return nhiData;
            }
        }
        throw new CardException("未找到健保卡。");
    }

    public static void main(String[] args) {
        NHIReader reader = new NHIReader();
        try {
            NHIData data = reader.readNHIData();
            System.out.println(data);
        } catch (CardException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}

class NHIData {
    private String cardNumber;
    private String name;
    private String idNumber;
    private String birthDate;
    private String gender;


    // 建構函式
    public NHIData(String cardNumber, String name, String idNumber, String birthDate, String gender) {
        this.cardNumber = cardNumber;
        this.name = name;
        this.idNumber = idNumber;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    // Getter 方法
    public String getCardNumber() {
        return cardNumber;
    }

    public String getName() {
        return name;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getGender() {
        return gender;
    }


    @Override
    public String toString() {
        return  
                "卡號：" + cardNumber + "\n" +
                "姓名：" + name +"\n"+
                "身分證：" + idNumber  +"\n" +
                "生日：" + birthDate  + "\n" +
                "性別：" + gender 
                ;
    }
}
