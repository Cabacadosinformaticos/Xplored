package pt.iade.ei.xplored.models

import android.content.Context
import com.google.android.gms.maps.model.LatLng

/**
 * A repository for managing Place objects, with persistence.
 */
object PlaceRepository {
    private var places: MutableList<Place> = mutableListOf()
    private var isInitialized = false
    private const val PREFS_NAME = "XploredPlacePrefs"
    private const val KEY_INITIAL_DATA_LOADED = "initialDataLoaded"

    /**
     * Initializes the repository with places from persistence.
     * This should be called once, at the start of the application.
     */
    fun initialize(context: Context) {
        if (!isInitialized) {
            places = PlacePersistence.loadPlaces(context)
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val initialDataLoaded = prefs.getBoolean(KEY_INITIAL_DATA_LOADED, false)

            if (!initialDataLoaded) {
                addInitialMockData(context)
                prefs.edit().putBoolean(KEY_INITIAL_DATA_LOADED, true).apply()
            }
            isInitialized = true
        }
    }

    /**
     * Returns the complete list of places.
     */
    fun getPlaces(): List<Place> = places

    /**
     * Adds a new place to the repository and saves the updated list.
     */
    fun addPlace(context: Context, place: Place) {
        places.add(place)
        PlacePersistence.savePlaces(context, places)
    }

    /**
     * Removes a place from the repository and saves the updated list.
     */
    fun removePlace(context: Context, placeId: String) {
        places.removeAll { it.id == placeId }
        PlacePersistence.savePlaces(context, places)
        // Optional: also remove photos for this place if you want full cleanup later
        // (not required for now since PhotoRepository has no delete API yet)
    }

    /**
     * Seed data (places only). Any photos are saved as PhotoItem rows
     * in PhotoRepository with placeId=<place.id> (no embedded URIs on Place).
     */
    private fun addInitialMockData(context: Context) {
        // 1) Build places (no photoUris on the data class anymore)
        val torre = Place(
            name = "Torre de Belém",
            description = "A beautiful tower on the Tagus river.",
            latLng = LatLng(38.6916, -9.2159),
            category = "Históricos",
            authorId = "1",
            address = "Av. Brasília, 1400-038 Lisboa",
            rating = 4.7,
            isVerified = true
        )
        val oceanario = Place(
            name = "Oceanário de Lisboa",
            description = "A stunning aquarium with a vast collection of marine life.",
            latLng = LatLng(38.7635, -9.0935),
            category = "Atividades",
            authorId = "1",
            address = "Esplanada Dom Carlos I s/nº, 1990-005 Lisboa",
            rating = 4.8,
            isVerified = true
        )

        // 2) Add places first (so they have stable ids)
        places.addAll(listOf(torre, oceanario))
        PlacePersistence.savePlaces(context, places)

        // 3) Persist their photos into PhotoRepository
        insertPlacePhotos(
            context = context,
            placeId = torre.id,
            authorId = torre.authorId,
            urls = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/4/44/Torre_de_Bel%C3%A9m_-_Lisbon_%282009%29.jpg/800px-Torre_de_Bel%C3%A9m_-_Lisbon_%282009%29.jpg",
                "https://www.visitlisboa.com/images/mediateca/xl/TorreBelem-1.jpg"
            )
        )
        insertPlacePhotos(
            context = context,
            placeId = oceanario.id,
            authorId = oceanario.authorId,
            urls = listOf(
                "https://www.oceanario.pt/media/filer_public/39/3a/393a38a7-21a4-448c-8433-41c305989e1a/gopro_hero_12_black_-_dive_housing_-_photo_mode_-_photo_settings_-_superphoto_-_output_-_raw_-_underwater_color_correction_1.jpg"
            )
        )
    }

    private fun insertPlacePhotos(
        context: Context,
        placeId: String,
        authorId: String,
        urls: List<String>
    ) {
        urls.forEach { url ->
            PhotoRepository.insert(
                context,
                PhotoItem(
                    reviewId = "PLACE-$placeId",
                    placeId = placeId,
                    userId = authorId,
                    url = url,
                    status = PhotoStatus.APPROVED,   // seed data is approved
                    // kind defaults to GALLERY
                )
            )
        }
    }
}
