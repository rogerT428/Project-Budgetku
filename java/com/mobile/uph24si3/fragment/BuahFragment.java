package com.mobile.uph24si3.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.mobile.uph24si3.DetailBuahActivity;
import com.mobile.uph24si3.R;
import com.mobile.uph24si3.adapter.BuahAdapter;
import com.mobile.uph24si3.model.Buah;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BuahFragment extends Fragment {

    private ListView listViewBuah;
    private BuahAdapter adapter;
    private List<Buah> buahList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buah, container, false);

        listViewBuah = view.findViewById(R.id.fragmentListViewBuah);

        buahList = getBuahList();
        adapter = new BuahAdapter(requireContext(), buahList);
        listViewBuah.setAdapter(adapter);

        listViewBuah.setOnItemClickListener((parent, v, position, id) -> {
            Buah buah = buahList.get(position);
            Intent intent = new Intent(requireContext(), DetailBuahActivity.class);
            intent.putExtra("nama", buah.getNama());
            intent.putExtra("emoji", buah.getEmoji());
            intent.putExtra("asal", buah.getAsal());
            intent.putExtra("rasa", buah.getRasa());
            intent.putExtra("deskripsi", buah.getDeskripsi());
            intent.putStringArrayListExtra("manfaat", new ArrayList<>(buah.getManfaat()));
            startActivity(intent);
        });

        return view;
    }

    private List<Buah> getBuahList() {
        List<Buah> list = new ArrayList<>();

        list.add(new Buah("Jeruk", "🍊", "Asia Selatan", "Manis-asam segar",
                "Jeruk adalah buah citrus yang kaya vitamin C. Buah bulat berwarna oranye ini termasuk dalam keluarga Rutaceae dan banyak dikonsumsi segar maupun dibuat jus.",
                Arrays.asList("Meningkatkan imunitas tubuh", "Mencegah anemia", "Menjaga kesehatan kulit", "Sumber vitamin C terbaik")));

        list.add(new Buah("Pisang", "🍌", "Asia Tenggara", "Manis lembut",
                "Pisang adalah buah tropis yang kaya akan kalium dan energi. Buah berwarna kuning ini sangat populer di seluruh dunia dan mudah dikonsumsi kapan saja.",
                Arrays.asList("Sumber energi cepat", "Menjaga kesehatan jantung", "Melancarkan pencernaan", "Mengurangi kram otot")));

        list.add(new Buah("Mangga", "🥭", "Asia Selatan", "Manis harum",
                "Mangga adalah raja buah tropis yang berasal dari India. Buah berdaging tebal ini memiliki rasa manis yang khas dan aroma yang harum memikat.",
                Arrays.asList("Meningkatkan kekebalan tubuh", "Menjaga kesehatan mata", "Membantu pencernaan", "Kaya antioksidan")));

        list.add(new Buah("Anggur", "🍇", "Asia Barat", "Manis-asam",
                "Anggur adalah buah kecil yang tumbuh dalam tandan. Buah ini kaya resveratrol, antioksidan kuat yang baik untuk kesehatan jantung dan umur panjang.",
                Arrays.asList("Menjaga kesehatan jantung", "Melawan radikal bebas", "Meningkatkan kesehatan otak", "Mencegah kanker")));

        list.add(new Buah("Stroberi", "🍓", "Eropa & Amerika", "Manis-asam segar",
                "Stroberi adalah buah berwarna merah cerah yang kaya vitamin C dan antioksidan. Buah ini sering digunakan dalam dessert, selai, dan minuman segar.",
                Arrays.asList("Kaya antioksidan", "Menjaga kesehatan kulit", "Meningkatkan daya ingat", "Mengontrol gula darah")));

        list.add(new Buah("Nanas", "🍍", "Amerika Selatan", "Manis-asam tropik",
                "Nanas adalah buah tropis yang mengandung enzim bromelain, yang membantu pencernaan protein. Buah berduri ini memiliki daging kuning yang segar dan manis.",
                Arrays.asList("Membantu pencernaan protein", "Anti-inflamasi alami", "Meningkatkan imunitas", "Mempercepat penyembuhan luka")));

        list.add(new Buah("Semangka", "🍉", "Afrika", "Manis segar",
                "Semangka adalah buah yang mengandung 92% air, menjadikannya pilihan sempurna untuk hidrasi di hari panas. Daging merahnya kaya likopen dan vitamin A.",
                Arrays.asList("Menjaga hidrasi tubuh", "Baik untuk jantung", "Mencegah kanker", "Meredakan nyeri otot")));

        list.add(new Buah("Melon", "🍈", "Asia & Afrika", "Manis lembut",
                "Melon adalah buah dengan daging yang lembut dan manis, kaya akan vitamin A dan C. Buah ini memiliki kandungan air yang tinggi dan cocok dikonsumsi saat cuaca panas.",
                Arrays.asList("Menjaga kesehatan mata", "Meningkatkan imunitas", "Menjaga kelembaban kulit", "Mengontrol tekanan darah")));

        list.add(new Buah("Pepaya", "🧡", "Amerika Tengah", "Manis lembut",
                "Pepaya adalah buah tropis yang kaya enzim papain, sangat baik untuk pencernaan. Dagingnya berwarna oranye cerah dan mengandung banyak vitamin A, C, dan folat.",
                Arrays.asList("Melancarkan pencernaan", "Mempercepat penyembuhan luka", "Menjaga kesehatan jantung", "Anti-inflamasi alami")));

        list.add(new Buah("Durian", "🟡", "Asia Tenggara", "Manis-pahit kuat",
                "Durian dijuluki 'Raja Buah' di Asia Tenggara. Meski berbau tajam, dagingnya sangat lezat dan kaya nutrisi termasuk vitamin B kompleks, potasium, dan serat.",
                Arrays.asList("Sumber energi tinggi", "Meningkatkan sistem imun", "Menjaga kesehatan tulang", "Baik untuk kesehatan reproduksi")));

        list.add(new Buah("Apel", "🍎", "Asia Tengah", "Manis-asam renyah",
                "Apel adalah salah satu buah paling populer di dunia. Kaya serat, vitamin C, dan antioksidan quercetin yang baik untuk kesehatan jantung dan otak.",
                Arrays.asList("Menjaga kesehatan jantung", "Membantu menurunkan berat badan", "Meningkatkan kesehatan usus", "Mencegah diabetes tipe 2")));

        list.add(new Buah("Pir", "🍐", "Asia & Eropa", "Manis lembut",
                "Pir adalah buah yang kaya serat larut, terutama pektin, yang baik untuk kesehatan pencernaan. Buah ini juga mengandung vitamin C, K, dan berbagai mineral penting.",
                Arrays.asList("Melancarkan pencernaan", "Menjaga kesehatan jantung", "Mengontrol berat badan", "Meningkatkan sistem imun")));

        list.add(new Buah("Ceri", "🍒", "Eropa & Amerika", "Manis-asam",
                "Ceri adalah buah kecil berwarna merah tua yang kaya antosianin dan melatonin. Buah ini dikenal memiliki sifat anti-inflamasi dan membantu kualitas tidur.",
                Arrays.asList("Meningkatkan kualitas tidur", "Anti-inflamasi", "Meredakan nyeri sendi", "Kaya antioksidan")));

        list.add(new Buah("Persik", "🍑", "Cina", "Manis-asam harum",
                "Persik adalah buah berbulu halus yang berasal dari Cina. Buah ini kaya vitamin A, C, E, dan K, serta serat yang baik untuk kesehatan pencernaan.",
                Arrays.asList("Menjaga kesehatan kulit", "Meningkatkan penglihatan", "Mendukung kesehatan jantung", "Membantu pencernaan")));

        list.add(new Buah("Kelapa", "🥥", "Asia Tropis", "Manis segar",
                "Kelapa adalah buah serbaguna yang hampir setiap bagiannya bermanfaat. Air kelapa kaya elektrolit, sedangkan dagingnya mengandung lemak sehat dan mineral penting.",
                Arrays.asList("Menghidrasi tubuh secara alami", "Mendukung kesehatan otak", "Meningkatkan metabolisme", "Menjaga kesehatan tulang")));

        list.add(new Buah("Kiwi", "🥝", "Cina & Selandia Baru", "Asam-manis segar",
                "Kiwi adalah buah kecil berbulu yang mengandung lebih banyak vitamin C dari jeruk. Daging hijaunya kaya vitamin K, folat, dan potasium yang luar biasa.",
                Arrays.asList("Meningkatkan imunitas tubuh", "Membantu pembekuan darah", "Menjaga kesehatan jantung", "Membantu pencernaan")));

        list.add(new Buah("Lemon", "🍋", "Asia Selatan", "Asam segar",
                "Lemon adalah buah citrus yang sangat asam dan kaya vitamin C. Sering digunakan sebagai penyedap masakan dan minuman, lemon juga memiliki sifat antibakteri.",
                Arrays.asList("Meningkatkan imunitas", "Membantu detoksifikasi", "Menjaga kesehatan kulit", "Membantu penyerapan zat besi")));

        list.add(new Buah("Alpukat", "🥑", "Amerika Tengah", "Gurih lembut",
                "Alpukat adalah buah unik yang kaya lemak tak jenuh tunggal, terutama asam oleat. Buah ini juga mengandung lebih banyak potasium dari pisang dan kaya serat.",
                Arrays.asList("Menjaga kesehatan jantung", "Membantu penyerapan nutrisi", "Melancarkan pencernaan", "Menjaga kesehatan mata")));

        list.add(new Buah("Blueberry", "🫐", "Amerika Utara", "Manis-asam",
                "Blueberry adalah buah kecil berwarna biru-ungu yang dikenal sebagai superfood. Kaya antosianin, buah ini memiliki kemampuan antioksidan tertinggi di antara buah-buahan.",
                Arrays.asList("Meningkatkan fungsi otak", "Melawan penuaan dini", "Menjaga kesehatan jantung", "Mengontrol gula darah")));

        list.add(new Buah("Delima", "❤️", "Persia & Asia Barat", "Manis-asam",
                "Delima adalah buah kuno yang kaya punicalagin dan punicic acid, antioksidan unik yang sangat kuat. Buah ini telah digunakan sebagai obat tradisional selama ribuan tahun.",
                Arrays.asList("Melawan peradangan", "Menurunkan tekanan darah", "Meningkatkan kesehatan jantung", "Membantu melawan kanker")));

        list.add(new Buah("Rambutan", "🔴", "Asia Tenggara", "Manis-asam",
                "Rambutan adalah buah tropis berbulu merah yang berasal dari Asia Tenggara. Dagingnya putih transparan dan berair, mengandung vitamin C dan zat besi yang tinggi.",
                Arrays.asList("Meningkatkan imunitas", "Sumber zat besi", "Menjaga kesehatan kulit", "Membantu pembentukan sel darah")));

        list.add(new Buah("Manggis", "🟤", "Asia Tenggara", "Manis-asam menyegarkan",
                "Manggis dijuluki 'Ratu Buah' karena rasanya yang luar biasa. Kulit buah ini mengandung xanthone, antioksidan paling kuat yang dikenal dalam dunia medis.",
                Arrays.asList("Anti-kanker alami", "Meningkatkan sistem imun", "Anti-inflamasi kuat", "Menjaga kesehatan jantung")));

        list.add(new Buah("Salak", "🟫", "Indonesia", "Manis-asam sepat",
                "Salak adalah buah asli Indonesia yang kulitnya menyerupai sisik ular. Dagingnya berwarna putih kekuningan dan kaya akan tanin, saponin, dan flavonoid.",
                Arrays.asList("Menjaga kesehatan mata", "Mengontrol gula darah", "Menjaga kesehatan usus", "Sumber energi alami")));

        list.add(new Buah("Jambu Biji", "🟢", "Amerika Tropis", "Manis-asam",
                "Jambu biji adalah buah tropis yang mengandung 4x vitamin C lebih banyak dari jeruk. Seluruh bagian buah ini bermanfaat, dari daging hingga bijinya.",
                Arrays.asList("Meningkatkan imunitas", "Menurunkan gula darah", "Menjaga kesehatan jantung", "Melancarkan pencernaan")));

        list.add(new Buah("Belimbing", "⭐", "Asia Tropis", "Asam-manis segar",
                "Belimbing atau starfruit adalah buah unik berbentuk bintang bila dipotong. Kaya vitamin C dan antioksidan, buah ini segar dikonsumsi langsung maupun dijus.",
                Arrays.asList("Meningkatkan imunitas", "Menurunkan kolesterol", "Menjaga kesehatan jantung", "Membantu pencernaan")));

        list.add(new Buah("Markisa", "💜", "Amerika Selatan", "Asam-manis harum",
                "Markisa adalah buah yang dikenal karena aromanya yang kuat dan harum. Kaya vitamin C, A, dan serat, buah ini sering dijadikan sirup atau minuman segar.",
                Arrays.asList("Meningkatkan sistem imun", "Menenangkan saraf", "Meningkatkan kualitas tidur", "Baik untuk pencernaan")));

        list.add(new Buah("Sirsak", "💚", "Amerika Tengah", "Manis-asam creamy",
                "Sirsak adalah buah tropis berduri yang memiliki daging putih lembut dan beraroma kuat. Dikenal mengandung acetogenin yang dipercaya memiliki sifat anti-kanker.",
                Arrays.asList("Potensi anti-kanker", "Meningkatkan imunitas", "Meredakan peradangan", "Menjaga kesehatan saraf")));

        list.add(new Buah("Longan", "⚪", "Asia Selatan", "Manis lembut",
                "Longan atau kelengkeng adalah buah kecil berkulit coklat tipis dengan daging putih transparan yang manis. Kaya vitamin C, B1, B2, dan kalium yang baik untuk tubuh.",
                Arrays.asList("Meningkatkan fungsi otak", "Meredakan stres", "Menjaga kesehatan kulit", "Meningkatkan energi")));

        list.add(new Buah("Leci", "🔵", "Cina Selatan", "Manis harum",
                "Leci adalah buah berwarna merah muda dengan daging putih berair dan sangat harum. Mengandung vitamin C yang tinggi dan antioksidan seperti flavonoid dan proanthocyanidins.",
                Arrays.asList("Meningkatkan imunitas", "Menjaga kesehatan kulit", "Mendukung pencernaan", "Kaya antioksidan")));

        list.add(new Buah("Kurma", "🟠", "Timur Tengah", "Manis karamel",
                "Kurma adalah buah yang tumbuh di pohon palem dan sangat populer di Timur Tengah. Kaya gula alami, serat, mineral, dan vitamin, kurma adalah sumber energi instan yang luar biasa.",
                Arrays.asList("Sumber energi instan", "Menjaga kesehatan tulang", "Melancarkan pencernaan", "Meningkatkan sistem saraf")));

        return list;
    }
}
