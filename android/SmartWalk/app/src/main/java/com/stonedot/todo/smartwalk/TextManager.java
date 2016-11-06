package com.stonedot.todo.smartwalk;

import android.util.Log;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import static java.text.Normalizer.Form.NFKC;

/**
 * Created by komatsu on 2016/11/04.
 */

public class TextManager {
    private static final Character.UnicodeBlock[] photographBlocks = {
            Character.UnicodeBlock.DINGBATS,
            Character.UnicodeBlock.MISCELLANEOUS_TECHNICAL,
            Character.UnicodeBlock.GEOMETRIC_SHAPES,
            Character.UnicodeBlock.MISCELLANEOUS_SYMBOLS,
            Character.UnicodeBlock.MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS,
            Character.UnicodeBlock.EMOTICONS,
            Character.UnicodeBlock.TRANSPORT_AND_MAP_SYMBOLS,
            Character.UnicodeBlock.HIGH_SURROGATES,
            Character.UnicodeBlock.LOW_SURROGATES
    };
    private static HashMap<String, String> hashMap = new HashMap(){ //デフォルト容量16
        {put("…", "");}
        {put("～", "ー");}
    };
    private static Set<String> keys = hashMap.keySet();

    public static String extractSpeakableChars(String text) {
        String normalizedText = Normalizer.normalize(text, NFKC);
        StringBuilder sb = new StringBuilder();
        Log.d("TextManager", text);
        for(int i = 0; i < text.length(); i++ ){
            //絵文字じゃなかったら追加
            char c = text.charAt( i );
            Character.UnicodeBlock ub = Character.UnicodeBlock.of( c );
            if( !Arrays.asList(photographBlocks).contains( ub ) ) {
                if( keys.contains( String.valueOf(c) ) ){
                    sb.append( hashMap.get( String.valueOf(c) ) );
                }else{
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }
}
