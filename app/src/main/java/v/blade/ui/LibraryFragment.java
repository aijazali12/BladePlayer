package v.blade.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import v.blade.R;
import v.blade.databinding.FragmentLibraryBinding;
import v.blade.library.Album;
import v.blade.library.Artist;
import v.blade.library.Library;
import v.blade.library.LibraryObject;
import v.blade.library.Playlist;

public class LibraryFragment extends Fragment
{
    private FragmentLibraryBinding binding;
    private List<? extends LibraryObject> current;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
        binding = FragmentLibraryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.mainListview.setLayoutManager(new LinearLayoutManager(getActivity()));
        String title = ((MainActivity) requireActivity()).binding == null ? getString(R.string.artists) : ((MainActivity) requireActivity()).binding.appBarMain.toolbar.getTitle().toString();
        updateContent(title, null);

        return root;
    }

    /*
     * Update content to list 'replacing', or to root directory
     */
    private void updateContent(String title, List<? extends LibraryObject> replacing)
    {
        if(replacing == null)
        {
            /* we are going back to top directory : artists, albums, songs, playlists */
            if(title.equals(getString(R.string.artists)))
                current = Library.getArtists();
            else if(title.equals(getString(R.string.albums)))
                current = Library.getAlbums();
            else if(title.equals(getString(R.string.songs)))
                current = Library.getSongs();
            else if(title.equals(getString(R.string.playlists)))
                current = Library.getPlaylists();
        }
        else
        {
            current = replacing;
        }

        LibraryObjectAdapter adapter = new LibraryObjectAdapter(current, this::onMoreClicked, this::onViewClicked);
        binding.mainListview.setAdapter(adapter);
        if(((MainActivity) requireActivity()).binding != null)
            ((MainActivity) requireActivity()).binding.appBarMain.toolbar.setTitle(title);
    }

    private void onViewClicked(View view)
    {
        int position = binding.mainListview.getChildLayoutPosition(view);
        LibraryObject clicked = current.get(position);
        onElementClicked(clicked);
    }

    private void onElementClicked(LibraryObject element)
    {
        if(element instanceof Artist)
            updateContent(element.getName(), ((Artist) element).getAlbums());
        else if(element instanceof Album)
            updateContent(element.getName(), ((Album) element).getSongs());
        else if(element instanceof Playlist)
            updateContent(element.getName(), ((Playlist) element).getSongs());
        //TODO : if element is Song, change player playlist...
    }

    private void onMoreClicked(View view)
    {

    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }
}