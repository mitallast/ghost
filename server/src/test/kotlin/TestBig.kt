
import com.github.mitallast.ghost.crypto.pkc.RSAPrivateKey
import com.github.mitallast.ghost.crypto.utils.BigInteger as KBigInteger
import java.math.BigInteger as JBigInteger
import com.github.mitallast.ghost.crypto.utils.Strings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestBig {

    val encrypted = arrayOf(113, -26, 119, -116, 16, 59, -101, 60, 75, 57, -2, 95, -68, 53, 116, -95, -113, 56, -14, 43, 33, 88, -69, 59, 96, 23, -121, -24, 102, -24, -16, -97, 67, 120, -83, -62, 102, -52, -49, -84, -60, 111, -29, -79, 77, 67, -10, -83, -28, -119, -52, 38, -20, -69, -30, -62, 77, 34, -68, 115, -121, 32, -51, 91, 91, -21, 84, -63, 64, -122, 35, -35, -9, -88, 107, -32, -53, 84, 1, 98, 40, 7, -118, 114, 86, 80, 46, -24, 125, -54, 98, -43, -72, -30, -94, -119, 80, 97, 127, 8, -72, -79, -26, 74, 28, 63, 113, -97, -2, -2, -65, 119, 99, -51, -128, 8, -38, -36, -88, -11, -51, -33, -125, -65, -62, -112, 39, -19, -21, -28, 114, 125, 13, -123, -24, 18, 100, 103, -56, -45, -4, 80, -12, -21, -76, -44, 16, -73, 39, 27, -55, 44, -80, 49, 70, 123, -105, -125, -107, -26, -74, 117, 5, -56, 79, 74, 30, 79, 48, -102, -75, -52, -85, -37, 92, -65, 76, 112, 59, 43, 54, 104, -108, 90, -29, 11, 67, -69, 100, -76, -85, -114, -125, -96, 100, 86, 76, 27, -82, 100, 112, -87, -27, 6, -24, 21, 39, 125, 93, -73, 49, -106, -93, 84, -93, -22, 41, -51, -87, 119, 103, 46, -48, 15, 98, 112, -88, 24, -125, -76, 30, -41, -87, 45, -96, -36, -125, 23, -71, -70, -85, -98, -122, 57, -121, -26, -111, 66, -105, 79, 77, -50, 8, -3, -28, 16, 102, -89, -116, 26, -39, -33, -26, -12, -18, -121, 15, 21, 124, 67, -114, -3, -102, 122, -83, 105, -61, 22, 57, 91, 86, 90, 90, -75, 95, -25, -43, 108, -33, 123, 44, -81, -43, -90, -18, 78, -78, 50, 2, -114, 9, -12, 126, -123, -49, -100, 110, -72, 78, 72, 82, 108, 53, 101, 72, -40, -127, -9, 38, 19, -52, 4, 8, -80, -48, -55, -66, -101, -24, -102, 73, -97, 124, 64, 56, -58, 127, -65, -81, 21, 59, -90, 35, -80, 89, -82, 50, 118, -85, -56, 3, -37, -32, -45, 47, -39, 110, 27, -34, -10, 13, 23, -128, 103, -91, -45, -84, 117, -6, -68, 14, 1, 116, 112, 41, -48, -81, -95, 108, 119, 43, -96, -116, 17, 35, -104, 67, -103, 2, -85, 56, -90, 94, 33, -101, -21, 93, -49, 16, -67, 66, -67, -69, 54, -34, -125, -21, 13, 94, -66, -79, -50, -69, 54, -4, 115, -4, 113, 83, -70, 115, 120, 29, 96, -65, 59, -121, 119, 66, -16, 0, 80, 87, 118, -101, -80, 96, 91, -115, -32, -94, -49, -17, -42, 39, -121, 4, 104, -50, -17, 33, -7, -114, -61, -95, -65, 90, 57, -27, 82, 13, 119, -16, -82, -115, 88, 33, 76, 57, 60, 14, -112, -85, -61, -97, 24, 83, -112, -44, -67, 102, 21, -109, 89, -38, -11, 7, 3, 37, -98, 14, 77, -53, 58, -30, -69, 89, 39, 88, 107, 114, 107, 21, -107, 122, -103, 116, -93, 113, -89, 122, 99, -40, 11, 125, 13, -49, 20, -97, -67, -33, 87, -3, 53, -92, -60, 101, 39, 56, -119, -12, -65, 7, 19, 32, 14, -41, 39, -117, 96, -123, -120, -30, 19, -64, -80, -34, -98, 8, 71, -102, 103, 25, -128, -8, -60, 38, -70, 112, 97, 13, 56, 81, -59, -66, -75, 104, 38, -117, 78, -56, 104, -26, -4, -22, 51, 118, -21, -24, -121, -61, 14, 78, -15, -100, -96, -2, 93, -50, 97, -48, -78, -91, 72, 46, 75, 25, -107, -34, -112, -60, 113, 5, -12, -30, 24, 16, -87, -76, -59, -18, 111, -120, 100, 86, 93, -68, 91, 82, 119, -56, -66, -100, 13, 122, -87, 80, -1, -98, 126, -27, 58, 67, 65, 71, -76, 80, 108, 39, 44, 50, -112, 43, -70, -105, 55, -120, 37, -50, -63, 55, 112, 27, 62, 60, 92, 103, 29, 114, -98, -47, 31, -125, -5, -96, 115, 80, 60, -32, 42, 56, -24, -76, 33, 69, 124, -101, -121, 27, 83, -15, -79, 39, -35, -42, -8, -46, 23, 26, 78, 0, -20, -54, -122, -31, 50, -82, 62, -9, 41, -73, -85, -16, 25, 5, -89, -20, -6, -58, -24, 85, 82, 13, -59, -110, 97, -95, -67, -61, -98, -33, -35, -6, -128, -35, -92, -110, 127, 45, -30, 118, 47, 125, -111, 18, -71, -63, 48, 70, -86, -34, 27, -110, -46, -14, -72, 93, 106, 19, 113, 33, 98, 93, -26, 73, -8, -66, 7, 48, -18, -76, -89, 62, -21, -59, 29, 31, -77, 88, -108, -2, 89, 2, -17, 70, -116, -5, -97, -94, -27, 116, 8, 37, 29, -128, -20, -28, -88, 87, 106, -71, 88, 28, -66, -15, 93, 57, 43, 96, 36, 94, -84, 10, -16, 72, 34, -63, -112, -37, -123, -31, 3, -67, 31, 73, 82, 61, -118, -96, 51, 66, 103, -92, 94, 62, 7, -97, 76, -122, 118, 13, -21, -6, -116, 50, -124, -32, -61, 92, 69, -17, 52, -103, -115, -21, 74, -40, 29, 34, -42, 127, -128, 44, -79, -71, -14, 96, -1, -42, 87, 83, 14, -36, -63, 75, -79, 118, -48, 77, 25, 58, -4, 87, 86, 89, 102, -113, 105, 106, -86, -44, -47, -87, 44, 2, -63, -19, 101, -15, 64, 39, -5, -106, -81, -80, -115, -43, 115, 50, 120, 3, 73, 118, 108, -61, -1, -92, 53, 35, -85, 53, -38, 82, -11, 116, -86, 50, -3, -66, 32, -27, -14, 93, 116, -15, 61, 23, -66, -74, 48, 111, -11, 124, -114, 65, 94, 72, -19, -80, 62, 58, -69, -7, -56, -36, 115, 43, -65, -123, 79, -3, 114, 75, 16, 73, 49, -72, 110, 56, -59, -93, -69, 98, -29, 5, -10, -110, 31, -32, 97, 44, -32, -69, 95, 37, 28, 5, -66, 18, -68, 19, 45, -112, 123, 68, -89, -63, 28, -98, -103, -60, -69, 118, -40, 50, 75, -122, -105, 14, 111, 28, 12, -94, -30, 91, 16, -2, 14, 51, 67, -122, -102, 111, -68, -35, -92, 116, -76, -46, 88, -103, 46, 13, -117)
        .map { it.toByte() }
        .toByteArray()

    val m = arrayOf(0, 2, 96, -76, 32, -69, 56, 81, -39, -44, 122, -53, -109, 61, -66, 112, 57, -101, -10, -55, 45, -93, 58, -16, 29, 79, -73, 112, -23, -116, 3, 37, -12, 29, 62, -70, -8, -104, 109, -89, 18, -56, 43, -51, 77, 85, 75, -16, -75, 64, 35, -62, -101, 98, 77, -23, -17, -100, 47, -109, 30, -4, 88, 15, -102, -5, 8, 27, 18, -31, 7, -79, -24, 5, -14, -76, -11, -16, -15, -48, 12, 45, 15, 98, 99, 70, 112, -110, 28, 80, 88, 103, -1, 32, -10, -88, 51, 94, -104, -81, -121, 37, 56, 85, -122, -76, 31, -17, -14, 5, -76, -32, 90, 0, 8, 35, -9, -117, 95, -113, 92, 2, 67, -100, -24, -10, 122, 120, 29, -112, -53, -26, -65, 26, -25, -14, -68, 64, -92, -105, 9, -96, 108, 14, 49, 73, -101, -16, 41, 105, -54, 66, -46, 3, -27, 102, -68, -58, -106, -34, 8, -6, 1, 2, -96, -3, 46, 35, 48, -80, -106, 74, -69, 124, 68, 48, 32, -34, 28, -83, 9, -65, -42, 56, 31, -5, -108, -38, -81, -69, -112, -60, -19, -111, -96, 97, 58, -47, -36, 75, 71, 3, -81, -124, -63, -42, 59, 26, -121, 105, 33, -58, -43, -122, -99, 97, -52, -71, -114, -47, 58, -26, -64, -102, 19, -4, -111, -31, 73, 34, -13, 1, -49, -117, -49, -109, 67, 21, -90, 4, -99, 47, 7, -39, -125, -6, -87, 27, -113, 78, 114, 101, -20, -72, 21, -89, -53, -85, -63, 69, 12, -73, 43, 60, 116, 16, 119, 23, -86, 36, -84, 66, -14, 91, 108, 103, -124, 118, 125, 14, 53, 70, -60, -9, 37, 1, -111, -93, -74, -86, -94, -74, 77, 18, 110, 85, -125, -80, 76, 17, 50, 89, -55, 72, -31, -48, -77, -101, -71, 86, 12, -43, 64, -101, 110, -54, -2, -37, -56, -84, -81, -18, -89, 77, -73, -8, 90, -33, -108, -66, -102, -123, -95, -35, 75, 3, -86, -120, -125, 29, -46, -100, 64, 120, -127, 11, 58, 40, -46, 45, 102, -128, -74, 79, -53, -79, -78, 55, -62, 68, 18, 52, -50, -85, -65, -38, -40, 124, 49, 21, 72, -10, 121, 2, 116, -71, 46, 106, 89, 29, 58, -79, -90, 11, 115, 64, 11, -60, 116, -59, 45, 60, -68, -14, -5, -82, 114, -74, -26, -44, -97, -80, -79, -123, 19, 54, -6, 44, 84, 12, -33, -65, 120, -56, -37, 73, 44, 101, -25, 91, 1, -14, 86, 10, -99, -60, 86, -2, -92, 3, 66, -122, 86, -98, 48, -122, -22, 100, -105, 36, -107, -100, 68, 8, -110, -38, -21, 114, 76, 6, -27, 19, 58, -55, -86, -108, 16, -18, -70, 45, 84, -2, -118, -3, -8, 80, 125, 33, 19, -30, 2, 106, -109, 122, -28, 57, -104, 43, -50, 121, -52, 36, 14, 38, 74, -10, -51, 67, -52, 48, 37, 102, 107, -71, -77, 127, -127, -25, 20, 21, 103, -83, 104, -55, -126, 73, -128, -37, -20, -51, 20, -37, 32, -88, 112, 88, 1, 113, 21, -74, 16, 82, 57, 102, 75, 118, -125, 96, -100, 24, -78, -26, 82, -38, -21, 107, -77, 120, -69, 63, 120, -128, 92, -69, -127, -74, -102, -7, -17, -3, 13, 66, 111, -111, -68, 18, 34, -8, 23, -119, -61, -112, -37, 40, 101, 90, 30, 2, 29, 96, -98, -99, 25, 12, -34, 85, -88, -55, 35, -19, 85, 18, -12, 72, -51, 0, 24, -105, 115, 7, 95, 78, -44, 28, 87, -49, -47, 123, -117, -66, 52, 2, 21, -71, -47, -8, -30, -70, 91, 123, 35, -56, 54, -3, 59, 89, -113, 89, -36, 21, 119, -60, -99, -105, 25, -72, -79, -109, 91, -128, 45, 32, -104, -93, 112, 27, 106, 65, 71, -34, 102, 86, -73, -3, 36, 79, 11, 33, 22, -66, 57, -23, 40, -39, -22, 84, 4, -70, -73, 105, 85, 13, -10, -116, 86, 48, -29, -37, -102, 2, -3, -11, -47, 49, -126, 27, -106, 90, -67, 48, -9, -47, -123, 115, 52, 22, -79, 69, 12, -19, -3, 3, -82, -39, 74, -68, -29, -43, 6, -8, -16, 0, -117, 40, 121, -67, 39, 22, -46, 78, 95, -115, -101, -74, -68, 127, 23, -120, 50, 15, 115, 77, 90, 104, 55, -21, -126, -15, -4, 79, 85, 60, -9, 70, -118, -117, 90, -16, -48, -91, -49, -33, 54, 19, 46, -59, -6, 49, 33, -100, 0, -93, -29, -117, 104, -108, -100, -24, 107, 6, 11, 38, -79, -68, 99, 11, 39, 114, 53, -6, -86, -78, -113, -91, 54, -88, 3, 82, 2, -120, 46, 127, -124, -11, 39, 53, 8, 50, -73, -127, -60, 69, -53, 125, -35, -36, 75, -123, -91, 11, 85, 51, 102, -47, -55, 93, -116, 62, -25, 96, -70, 105, 70, 122, 120, 4, 111, -115, -36, 91, 55, -76, -60, 21, 73, 55, 1, -23, -96, 82, -39, 81, -96, 70, -70, -49, -5, 2, 21, -89, -13, -12, -83, 73, 119, -77, 108, 45, -35, -62, -52, 113, -49, -66, -111, 114, 114, -67, 43, 62, 119, -101, -84, 71, 73, 123, -34, 80, 46, 60, -28, 51, -27, -111, -121, 85, -104, 99, 68, -44, 96, 24, -126, -3, 85, -23, -55, 56, -47, 85, -54, 2, -38, 42, -99, -121, -75, -108, 16, 122, 0, 1, -64, 42, 13, -104, -99, 54, -48, 67, -73, 89, 5, 15, -19, -90, -113, -28, -3, 63, 51, -63, 49, 111, 45, 76, 60, 34, 122, 56, 16, -81, -52, 109, -93, 7, 81, 95, 98, 98, 3, -80, -46, 97, 62, 97, -62, 56, -109, 104, 49, 69, 75, -82, 26, -90, 0, 83, 99, 104, 119, 101, 105, -61, -97, 103, 101, 113, 117, -61, -92, 108, 116, 32, 122, -61, -68, 110, 100, 101, 116, 32, 84, 121, 112, 111, 103, 114, 97, 102, 32, 74, 97, 107, 111, 98, 32, 118, 101, 114, 102, 108, 105, 120, 116, 32, -61, -74, 100, 101, 32, 80, 97, 110, 103, 114, 97, 109, 109, 101, 32, 97, 110, 46)
        .map { it.toByte() }
        .toByteArray()

    val c = "485298820449666651127573742396314567971727653163728729001815758601589926335881577560478458624295174166653609997444057211112461693527563473842755021134715412676676821687843735597078030667667619610210117931567790707549216902884659441889162141029146200067513342833094054212682288865553475393126669742031627021682522757837948135031868166588462286172865054207292206262410273794462238417809400510854609669499137123408137828187010237909479195849140551428692575431911851281537474335272782353274452674322502301872135888976032412224186260154332939855858003817389017365102192621579782262144429190936723720322479018003963580440396459688920638610834129669257335046967570150286978315295321406141142856979166449295930595518878739271177031897649178996225738140108544064998768376030238397474551627712534239840245992320053220876517847833404199373876307327518507229050560284908068404741335421581502797434045550735656511258344375461100925659445609262653162551560549806745236232559648614290381789762382596061711310474657010418386079074131283699407990317139983650941254172847120651053092753594324431650672702940661736257352732610491595911143179337771565226031576759540862629795554284200521996070853585700609021307442084385368500630216136094519293591484356551572962792722248570136329843839170338530726884379811136509067876979191055684764396669213266172596576316532977086174128621263070865992521887189024684162963768607275012731730196734733339852505024590637388757072589542619889383281773570442637833687327645628749960626831910926169081107154470113258062379499119174981455858626488443058010640391705694545104057718545357496435096454525600460813139830013159621107351073196205905181886906337476243944035697423905845452589822593628386089221752079730788958947010125429911064134103675406976022526630757686923370099895022971494229966083851323971612685630946069356458156885826186864675450790097869745125308496442461468377846310343930760587985939953582024110792428571627690964706927497869797105196978733501370636407767379393243876742029957924216616883288593281621282630995242782317677073519615941660349196509706221912704943698029033387415688108881776530090011011183055521805651267199809194878007321153038143730742073019264614676157512488472134482542517980167290272538267148510814833604599472900194746160537215339696540435923859551601066281605025036315931141861857127896324747633868007971109174736151654877021540982599174500516333461852649292261818624150133849891323340678257105622156429613140413835"
    val pm = "39574047535847534741501520902448480040163006596401241524194758668102698139729909039789252475681020243431353069981725308148801591321454147973504451181852820141520151537305258058138068199428512167220565809418500217928927500229833207181588865205156874018329955768521437375628752623520666624842340952825267204265033225063353853610820998956666674047204561026435490240959803021408458165901961440363118267771478024029077588344025998242444910060994525106027981522633343049070567037139925501240397949165889642349020530119038343529497046169561374481387575446155343992223478467943523867226584542226864100721724600549351743460551750276986928716597714417646461566107845421629712336613130475254641194529579767445825735945031112477290729858171562953102400829909705898548497620882707887002657516982161732752070351917160336702281517775229390853733833541567869855237873856489685774146243470149452002259599915658238720739875480478678418025151847110993719486599170434040951331121040938107340039774989117989848860852507551685387647519040617668594022063033838584625006565003495137747026053987920061534896451584432681106046613740717604074744073280999362728358250601180888634945760770543243959812926538813380231252596926406916097225912110581592118208106207780309964372020302431361047492146150894981976149603426503717141518021119390469768302902321040915872930922969163854003394718917100209433249938109176874756995567072805044848563031385324112085869930109302583568082288127067886865599581159820204179358610026350920573071909738536441398742533312655158278640244161842023293367590771215335265153148270547667801221280931732503874007861249979966757827065327437529849657221178838354324241611452232164991579389557963600915017802351839832295988787123929621168606620990071627096809052473617306923324543584952875306487933535266879228260988697997330246475942317416403342660800439127595056372067910009113636778795391977584864996423554522718544280556747148774050231903396810390610197652736021144877238933674399625588177359555638221046883697876780989278832476539846008216330369422057128536843152381246369134270137605833026921840803486081634368077821357357019868324916940143248900358555458039167102125397400689647111656267145680046685937833147762890647614249313965263490783270780892709769308672025740302662884102377764846995128885948447394310498694814982563987074176804697986413991320503696879847519680149902226570152202897691033366036040589862796404519060714931376148647664928817799889631355315777070"

    val pkD = "108578389455409979832006468492198970933061430879856693998597324875307700048244887385439428342438259564655973994058057027915550425065988775407285694398008011081687996099792505902423802161458183257338333634233508458969586120658749775365722684140703678601131336769984968540244838009985267317536226532732861059991562528210849948228269725093101919542249310963246924860674596588084192204574466821468041710254840482691211981762301705503154869832504370289130699902432418069352660605066460726417901219110525798709609939392382008079619643157295617984222331665787883655173345985991697710806982151469059762838267959064773270312339173124420310743970070999882296380470020815853372891963703970706440412265033816741529006967746750778953353804864471813197149218248966540663489884023399807528157486499622562352735696837173315637008446323801073196584226862754269444952362116600805740602701237778046179589771997906648221788349659969883544519253991345112450693308720658363852739275954067387254751734742596814909131968009433563149207332698386327657634151147826980235913953767219345715008370624266313787711819992458769359360599750003797673573598997971747541504772167281070516631992508450539652309008289706533565915674632525637730884729314081050395130926146465667218610142703594270143361780674618836702845286903331350523296362470018830273867806503276297449068755298269564074622055134959893764825871911866311307173877245430739982120576343648478252524538876050052173244689615981974289292539582372000154291993415542002363506714117983539923408486833221514088183849934058824553387362573740546972350482054249363809612239775032940931830173186409155027127530157837617874831686993195603643930238270851679084805920252553823501211959278491174762976214553818582366466955666827451156902770029346930377815843814788172573180978868420257874178419980537170074939780854677000314409215484860976205284069162327202249489238613244289205109012653232078593175339166753663094951958188276474145676786515999719232114160200916176730424714160548878942752115335890848208337046711429691158904289262856676643181264657263386195638522877937717939888373198340071877510513770240893976289384216547893914456567919163283008301377562698321241633897784446234846108707885017339723984302413106473038378693397399411713199900881367983194015542100110616469165645068507927630496424221463331050816346788670492836048061603316461409772531202727189118317810404917215662379111379161395380143890950135891925861203777564570044392012447763369469"
    val pkN = "875634407900394451200212915704625094307193900855369511878678016678575219609244323434593687770793933631459422702311298724146472749329827641952714306091249563320021077415271790346050625876012900366837052470904437252430978999976401252527460940858118195398281393853822648707858155121159572035296541617364717498786075572960698438171171202311194069566617407828173126060066110534119213670422355949927624900031348876255314185729632350872240546420949592912699874423476733175466547931795802165579669447896729163568413206774229825891414038486278959814417878156695347588558020477228119813155797277484703955990780978875293102966481194374170176439609207455045229991218095373160811762375170338536824635117370978609614027599334069176520147226365120298044754705708965884039805680782606315753302212986502097299451510368659145874066128974138552238378113840390809091966832319915528742545944485811271078035494419926563441878933075563926143253571980086120870969869565739835940492241640884146760943832962850991708760009114002911051925930610839038097425994628210423900424035066538090519018514302056733824095751002487274681753253806351220642482499120509681835376695876992156408852009131040023471315799866706050255745159342815232591377406466765715876171711816052329738115549673220970017004958089928263876929442041075014388179487174486656262377685173156308605107061292019130378997305526072769771152552050049343455587891147103093659176601769179287567459767009119447838997099315975909568160229908228510841486677661109240345177393406097476470236448176764612358207518296416604929578550266601820052991260243271443067050961386885359421843807426091845337765796673556773231230869417949715027700953532167961002778727523713340508861034358629657814656916258693693768734728241134934569257278630401482048493833475236079664196561884892266242539658053230535837095169643202378143041244558893343497202298740926105764092838093815875047831920372572158645421564410487989660712180177358913061734792837595343736083776317444645400032667310395195335860878576429897754405074306250523944105073187446714342015504491517337267680623203109366594218062852687489685502979162930375945499956810443922400668872643638780053126493447267319852772150405696624511257001835385144663010991867305609539436489296433748227183121975591491919235211072779988604298480479003408175858538476173459192436803665051448955893812831409822643085310728366394216635457074612735182450727538935827021184467656861363160450941192924416545561724765725104619"
    val pkK = 1024

    @Test
    fun test() {
        val cc = os2ip(encrypted)
        assertEquals(c, cc.toString())
        val pmm = decryptBlock(cc)
        assertEquals(pm, pmm.toString())
        val mm = i2osp(pmm, pkK)
        //val mm = i2ospJ(java.math.BigInteger(pm), pkK)
        assertTrue(m.contentEquals(mm))

        println(Strings.fromByteArray(decode(mm)))
    }

    private fun decode(message: ByteArray): ByteArray {
        val index = message.lastIndexOf(0x00)
        return message.copyOfRange(index + 1, message.size)
    }

    private fun decryptBlock(m: KBigInteger): KBigInteger {
        return m.modPow(KBigInteger(pkD), KBigInteger(pkN))
    }


    private fun os2ip(bi: ByteArray): KBigInteger {
        var out = KBigInteger.ZERO
        val max = KBigInteger.valueOf(256)

        for (i in 1..bi.size) {
            out = out.add(KBigInteger.valueOf((0xFF and bi[i - 1].toInt()).toLong()).multiply(max.pow(bi.size - i)))
        }
        return out
    }

    fun i2osp(bi: KBigInteger, len: Int): ByteArray {
        val twofiftysix = KBigInteger.valueOf(256)
        val out = ByteArray(len)
        var cur: Array<KBigInteger>

        if (bi >= twofiftysix.pow(len)) {
            throw IllegalArgumentException("integer to large")
        }

        for (i in 1..len) {
            val tp = twofiftysix.pow(len - i)
            val jtp = JBigInteger("256").pow(len - i)

            assertEquals(tp.toString(), jtp.toString())

            if(i == 686){
                println()
            }
            cur = bi.divideAndRemainder(tp)
            val curJ = JBigInteger(bi.toString()).divideAndRemainder(jtp)

            // 4095455352133949855963341973643501722157779477990387891761269486050812386237509662992972870720919896547255976780098979184888911241746879017762560351976824341727575424001602329276928916746397727053501645026940607820727134212320498175189039370563089006835643486727603160976281128551447849394563822925437548534721245059516457622028413926081940506240445959534748348783863103090058211036508800794370121352870959551199715894533706022412599425386525847382344291628089865265134136454534708484909263082999997211159382782975108069962205684679256532480761364767882405144475542710840094623690847356910607397033592954846089748443160220527694333884633697165214660022684053345676613141293860441378027072950599631049591672874689913765525263607766898151929977952777887157113817261058395485429107306211402200631808326577202779716892593516822311939123461603144306444174360422037279180402797475155608799315846377830864737060137508638803622713950424329514576208154359232474655731273594946204829342525892271178952730517381031881876242349024924471146915127932336708642172204787361391629998453493505982305702633558947312168659444883215584804995811045765768347225057684603333491008189785214813922827196392562684754334384280298022516158571613822733635555675195009028202354069070740910634551408056123264488340472424795311457815015649792650835757622229518541157249039119916033112935918248032522341826790963390187154435705714239592465799164255026014571474768013346161306168907385886198132001280503952153884910585756917538958593329275059748794899682664716486415103336047252002720820290685135566224733966132233362902394244519246248770852532743871926345468572829646771937342914929
            // 4095455352133949855963341973643501722157779477990387891761269486050812386237509662992972870720919896547255976780098979184888911241746879017762560351976824341727575424001602329276928916746397727053501645026940607820727134212320498175189039370563089006835643486727603160976281128551447849394563822925437548534721245059516457622028413926081940506240445959534748348783863103090058211036508800794370121352870959551199715894533706022412599425386525847382344291628089865265134136454534708484909263082999997211159382782975108069962205684679256532480761364767882405144475542710840094623690847356910607397033592954846089748443160220527694333884633697165214660022684053345676613141293860441378027072950599631049591672874689913765525263607766898151929977952777887157113817261058395485429107306211402200631808326577202779716892593516822311939123461603144306444174360422037279180402797475155608799315846377830864737060137508638803622713950424329514576208154359232474655731273594946204829342525892271178952730517381031881876242349024924471146915127932336708642172204787361391629998453493505982305702633558947312168659444883215584804995811045765768347225057684603333491008189785214813922827196392562684754334384280298022516158571613822733635555675195009028202354069070740910634551408056123264488340472424795311457815015649792650835757622229518541157249039119916033112935918248032522341826790963390187154435705714239592465799164255026014571474768013346161306168907385886198132001280503952153884910585756917538958593329275059748794899682664716486415103336047252002720820290685135566224733966132233362902394244519246248770852532743871926345468572829646771937697018902

            // 4095455352133949855963341973643501722157779477990387891761269486050812386237509662992972870720919896547255976780098979184888911241746879017762560351976824341727575424001602329276928916746397727053501645026940607820727134212320498175189039370563089006835643486727603160976281128551447849394563822925437548534721245059516457622028413926081940506240445959534748348783863103090058211036508800794370121352870959551199715894533706022412599425386525847382344291628089865265134136454534708484909263082999997211159382782975108069962205684679256532480761364767882405144475542710840094623690847356910607397033592954846089748443160220527694333884633697165214660022684053345676613141293860441378027072950599631049591672874689913765525263607766898151929977952777887157113817261058395485429107306211402200631808326577202779716892593516822311939123461603144306444174360422037279180402797475155608799315846377830864737060137508638803622713950424329514576208154359232474655731273594946204829342525892271178952730517381031881876242349024924471146915127932336708642172204787361391629998453493505982305702633558947312168659444883215584804995811045765768347225057684603333491008189785214813922827196392562684754334384280298022516158571613822733635555675195009028202354069070740910634551408056123264488340472424795311457815015649792650835757622229518541157249039119916033112935918248032522341826790963390187154435705714239592465799164255026014571474768013346161306168907385886198132001280503952153884910585756917538958593329275059748794899682664716486415103336047252002720820290685135566224733966132233362902394244519246248770852532743871926345468572829646771937342914929
            // 4095455352133949855963341973643501722157779477990387891761269486050812386237509662992972870720919896547255976780098979184888911241746879017762560351976824341727575424001602329276928916746397727053501645026940607820727134212320498175189039370563089006835643486727603160976281128551447849394563822925437548534721245059516457622028413926081940506240445959534748348783863103090058211036508800794370121352870959551199715894533706022412599425386525847382344291628089865265134136454534708484909263082999997211159382782975108069962205684679256532480761364767882405144475542710840094623690847356910607397033592954846089748443160220527694333884633697165214660022684053345676613141293860441378027072950599631049591672874689913765525263607766898151929977952777887157113817261058395485429107306211402200631808326577202779716892593516822311939123461603144306444174360422037279180402797475155608799315846377830864737060137508638803622713950424329514576208154359232474655731273594946204829342525892271178952730517381031881876242349024924471146915127932336708642172204787361391629998453493505982305702633558947312168659444883215584804995811045765768347225057684603333491008189785214813922827196392562684754334384280298022516158571613822733635555675195009028202354069070740910634551408056123264488340472424795311457815015649792650835757622229518541157249039119916033112935918248032522341826790963390187154435705714239592465799164255026014571474768013346161306168907385886198132001280503952153884910585756917538958593329275059748794899682664716486415103336047252002720820290685135566224733966132233362902394244519246248770852532743871926345468572829646771937697018902

            println(i)

            assertEquals(cur[0].toString(), curJ[0].toString())
            assertEquals(cur[1].toString(), curJ[1].toString())

            out[i - 1] = cur[0].intValue().toByte()
        }
        return out
    }

    fun i2ospJ(bi: java.math.BigInteger, len: Int): ByteArray {
        val twofiftysix = java.math.BigInteger("256")
        val out = ByteArray(len)
        var cur: Array<java.math.BigInteger>

        if (bi >= twofiftysix.pow(len)) {
            throw IllegalArgumentException("integer to large")
        }

        for (i in 1..len) {
            cur = bi.divideAndRemainder(twofiftysix.pow(len - i))
            out[i - 1] = cur[0].toByte()
        }
        return out
    }
}