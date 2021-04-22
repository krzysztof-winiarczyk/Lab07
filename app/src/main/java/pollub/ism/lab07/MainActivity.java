package pollub.ism.lab07;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import pollub.ism.lab07.BazaMagazynowa;
import pollub.ism.lab07.PozycjaMagazynowa;
import pollub.ism.lab07.R;
import pollub.ism.lab07.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ArrayAdapter<CharSequence> adapter;

    String wybraneWarzywoNazwa = null;
    Integer wybraneWarzywoIlosc = null;

    private BazaMagazynowa bazaDanych;

    public enum OperacjaMagazynowa {SKLADUJ, WYDAJ};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //podłączenie adaptera do spinnera
        adapter = ArrayAdapter.createFromResource(this, R.array.Asortyment, android.R.layout.simple_dropdown_item_1line);
        binding.spinner.setAdapter(adapter);

        //podłączenie do bazy danych
        bazaDanych = Room.databaseBuilder(getApplicationContext(), BazaMagazynowa.class, BazaMagazynowa.NAZWA_BAZY)
                .allowMainThreadQueries().build();
        //jeśli baza jest pusta pobierz pozycje z strings array Asortyment i utwórzy rekordy w bazie
        if(bazaDanych.pozycjaMagazynowaDAO().size() == 0){
            String[] asortyment = getResources().getStringArray(R.array.Asortyment);
            for(String nazwa : asortyment){
                PozycjaMagazynowa pozycjaMagazynowa = new PozycjaMagazynowa();
                pozycjaMagazynowa.NAME = nazwa;
                pozycjaMagazynowa.QUANTITY = 0;
                bazaDanych.pozycjaMagazynowaDAO().insert(pozycjaMagazynowa);
            }
        }

        binding.btnSkladuj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zmienStan(OperacjaMagazynowa.SKLADUJ);
            }
        });

        binding.btnWydaj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zmienStan(OperacjaMagazynowa.WYDAJ);
            }
        });

        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
                wybraneWarzywoNazwa = adapter.getItem(i).toString();
                aktualizuj();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    private void aktualizuj(){

        wybraneWarzywoIlosc = bazaDanych.pozycjaMagazynowaDAO().findQuantityByName(wybraneWarzywoNazwa);
        binding.textStanMagazynu.setText("Stan magazynu dla " + wybraneWarzywoNazwa + " wynosi: " + wybraneWarzywoIlosc + " kg");
    }

    private void zmienStan(OperacjaMagazynowa operacja){

        Integer zmianaIlosci = null, nowaIlosc = null;

        //odczytaj int z pola edycyjnego
        try {
            zmianaIlosci = Integer.parseInt(binding.nmbrInptIlosc.getText().toString());
        }catch(NumberFormatException ex){
            return;
        }finally {
            binding.nmbrInptIlosc.setText("");
        }

        //w zależności od operacji oblicz nową ilość
        switch (operacja){
            case SKLADUJ:
                nowaIlosc = wybraneWarzywoIlosc + zmianaIlosci;
                break;
            case WYDAJ:
                if (wybraneWarzywoIlosc < zmianaIlosci){
                    Toast.makeText(this, "Nie ma tyle " + wybraneWarzywoNazwa, Toast.LENGTH_LONG).show();
                    nowaIlosc = wybraneWarzywoIlosc;
                }
                else {
                    nowaIlosc = wybraneWarzywoIlosc - zmianaIlosci;
                }
                break;

        }

        //update do bazy
        bazaDanych.pozycjaMagazynowaDAO().updateQuantityByName(wybraneWarzywoNazwa, nowaIlosc);

        //aktualizuj wyświetlane dane
        aktualizuj();
    }
}