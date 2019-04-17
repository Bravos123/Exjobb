package com.example.navigationtesting.SatelliteMVC;

import android.util.Log;

public class SatelliteNORADId {
    //Catalogen tagen från: https://www.celestrak.com/satcat/search.php
    private static final String galileoDatabasePart=
            "2018-060A    43564  *+ GSAT0221 (GALILEO 25)     ESA    2018-07-25  FRGUI                844.7   56.5   23239   23205     N/A    \n" +
            "2018-060B    43565  *+ GSAT0222 (GALILEO 26)     ESA    2018-07-25  FRGUI                844.7   56.5   23239   23205     N/A    \n" +
            "2018-060C    43566  *+ GSAT0219 (GALILEO 23)     ESA    2018-07-25  FRGUI                844.7   56.5   23238   23206     N/A    \n" +
            "2018-060D    43567  *+ GSAT0220 (GALILEO 24)     ESA    2018-07-25  FRGUI                844.7   56.5   23238   23206     N/A    \n" +
            "2017-079A    43055 M*+ GSAT0215 (GALILEO 19)     ESA    2017-12-12  FRGUI                844.7   56.6   23223   23222     N/A    \n" +
            "2017-079B    43056 M*+ GSAT0216 (GALILEO 20)     ESA    2017-12-12  FRGUI                844.7   56.6   23227   23217     N/A    \n" +
            "2017-079C    43057 M*+ GSAT0217 (GALILEO 21)     ESA    2017-12-12  FRGUI                844.7   56.6   23227   23218     N/A    \n" +
            "2017-079D    43058 M*+ GSAT0218 (GALILEO 22)     ESA    2017-12-12  FRGUI                844.7   56.6   23229   23216     N/A    \n" +
            "2016-069A    41859 M*+ GSAT0207 (GALILEO 15)     ESA    2016-11-17  FRGUI                844.7   54.6   23237   23207   18.3608  \n" +
            "2016-069B    41860 M*+ GSAT0212 (GALILEO 16)     ESA    2016-11-17  FRGUI                844.7   54.6   23234   23211    2.7896  \n" +
            "2016-069C    41861 M*+ GSAT0213 (GALILEO 17)     ESA    2016-11-17  FRGUI                844.7   54.6   23234   23210   14.1741  \n" +
            "2016-069D    41862 M*+ GSAT0214 (GALILEO 18)     ESA    2016-11-17  FRGUI                844.7   54.6   23232   23212    8.5742  \n" +
            "2016-030A    41549 M*+ GSAT0211 (GALILEO 14)     ESA    2016-05-24  FRGUI                844.7   56.6   23228   23216    6.5968  \n" +
            "2016-030B    41550 M*+ GSAT0210 (GALILEO 13)     ESA    2016-05-24  FRGUI                844.7   56.6   23224   23221   15.8866  \n" +
            "2015-079A    41174 M*+ GSAT0209 (GALILEO 12)     ESA    2015-12-17  FRGUI                844.7   54.9   23238   23207    6.7254  \n" +
            "2015-079B    41175 M*+ GSAT0208 (GALILEO 11)     ESA    2015-12-17  FRGUI                844.7   54.9   23235   23209    9.9514  \n" +
            "2015-045A    40889 M*+ GSAT0205 (GALILEO 9)      ESA    2015-09-11  FRGUI                844.7   56.5   23235   23209    9.5282  \n" +
            "2015-045B    40890 M*+ GSAT0206 (GALILEO 10)     ESA    2015-09-11  FRGUI                844.7   56.5   23230   23214    8.8826  \n" +
            "2015-017A    40544 M*+ GSAT0203 (GALILEO 7)      ESA    2015-03-27  FRGUI                844.7   56.1   23236   23208    8.4512  \n" +
            "2015-017B    40545 M*+ GSAT0204 (GALILEO 8)      ESA    2015-03-27  FRGUI                844.7   56.1   23228   23216    9.4592  \n" +
            "2014-050A    40128 M*+ GSAT0201 (GALILEO 5)      ESA    2014-08-22  FRGUI                776.2   50.6   26252   16947    4.1215  \n" +
            "2014-050B    40129 M*+ GSAT0202 (GALILEO 6)      ESA    2014-08-22  FRGUI                776.2   50.6   26250   16949    2.0000   ";

    public static final String getGalileoNORADId(int svid) throws NoradIdDoesNotExist {
      String[] databaseLines = galileoDatabasePart.split("\\n");

      for(String line : databaseLines){
          if(line.indexOf("GALILEO "+Integer.toString(svid)) != -1){
              String parsedLine = line.replaceAll(" [ \\t]+", " ");
              String noradId = parsedLine.split(" ")[1];
              return noradId;//Integer.parseInt(noradId);
          }
      }

      throw new NoradIdDoesNotExist("There is no stored NORAD ID for svid: "+Integer.toString(svid));


    };

    public static final String getGPSNORADId(int svid){
        return null;
    }


}
