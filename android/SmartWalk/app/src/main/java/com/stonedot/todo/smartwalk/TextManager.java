package com.stonedot.todo.smartwalk;

import android.util.Log;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.text.Normalizer.Form.NFKC;

/**
 * Created by komatsu on 2016/11/04.
 */

public class TextManager {
    private static final Character.UnicodeBlock[] photographBlocks = {
            Character.UnicodeBlock.MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS,
            Character.UnicodeBlock.HIGH_SURROGATES,
            Character.UnicodeBlock.LOW_SURROGATES
    };

    public static String extractSpeakableChars(String text) {
        String normalizedText = Normalizer.normalize(text, NFKC);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < text.length(); i++ ){
            //絵文字じゃなかったら追加
            char c = text.charAt( i );
            Character.UnicodeBlock ub = Character.UnicodeBlock.of( c );
            if( !Arrays.asList(photographBlocks).contains( ub ) ){
                sb.append( c );
            }
        }
        return sb.toString();
    }
}
