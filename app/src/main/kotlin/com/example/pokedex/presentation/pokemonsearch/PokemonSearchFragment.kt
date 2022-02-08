package com.example.pokedex.presentation.pokemonsearch

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.domain.model.Pokemon
import com.example.pokedex.R
import com.example.pokedex.databinding.FragmentPokemonSearchBinding
import com.example.pokedex.presentation.base.BaseBindingFragment
import com.example.pokedex.utils.*
import com.google.android.material.bottomnavigation.BottomNavigationView

class PokemonSearchFragment :
    BaseBindingFragment<FragmentPokemonSearchBinding>(FragmentPokemonSearchBinding::inflate) {

    private val viewModel: PokemonSearchViewModel by viewModels {
        appComponent.viewModelsFactory()
    }
    private lateinit var pokemonImageUrl: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeUi()
        initViews()
    }

    @SuppressLint("SetTextI18n")
    private fun subscribeUi() {
        viewModel.pokemon.observe(viewLifecycleOwner) { pokemon ->
            pokemon ?: return@observe

            with(requireBinding()) {
                pokemonIdText.text = pokemon.id.toString()
                pokemonNameText.text = pokemon.name.capitalized()
                pokemonHeightText.text =
                    "${((pokemon.height * 100F) / 1000F)} M"
                pokemonWeightText.text =
                    "${((pokemon.weight * 100F) / 1000F)} KG"
                Glide.with(requireContext()).load(pokemon.imageUrl)
                    .into(pokemonImage)
                pokemonImageUrl = pokemon.imageUrl
            }
        }

        viewModel.dataLoading.observe(viewLifecycleOwner) { isLoading ->
            with(requireBinding()) {
                loadingPokemonProgressBar.isVisible = isLoading
                pokemonImage.isInvisible = isLoading
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                with(requireBinding()) {
                    pokemonImage.setImageResource(R.drawable.ic_not_found)

                    listOf(
                        pokemonIdText,
                        pokemonNameText,
                        pokemonWeightText,
                        pokemonHeightText
                    ).onEach { textView -> textView.text = "" }

                    Toast.makeText(
                        requireContext(), errorMessage, Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun initViews() {
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav?.visibility = View.VISIBLE

        requireBinding().apply {
            searchPokemonButton.setOnClickListener {
                if (!searchPokemonEdt.text.isNullOrEmpty()) {
                    val pokemonName = searchPokemonEdt.text?.trim().toString().lowercase()
                    viewModel.getPokemonByName(pokemonName)
                } else {
                    Toast.makeText(
                        requireContext(),
                        R.string.the_field_must_not_be_empty,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            addToFavoriteButton.setOnClickListener {
                if (pokemonIdText.text.isNotEmpty()) {
                    val pokemon = Pokemon(
                        pokemonIdText.text.toString().toInt(),
                        pokemonNameText.text.toString(),
                        pokemonHeightText.text.filter { char -> char.isDigit() }
                            .toString()
                            .toFloat() / 10F,
                        pokemonWeightText.text.filter { char -> char.isDigit() }
                            .toString()
                            .toFloat() / 10F,
                        pokemonImageUrl
                    )

                    viewModel.isAddedPokemonWithThisId(pokemon.id).observeOnce { isAdded ->
                        if (isAdded == true) {
                            Toast.makeText(
                                requireContext(),
                                R.string.this_pokemon_added_to_favorites,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            viewModel.addToFavorites(pokemon)
                            Toast.makeText(
                                requireContext(),
                                R.string.pokemon_has_been_added,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(), R.string.failed_to_add_pokemon, Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}