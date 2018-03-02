package eu.spod.isislab.spodapp.utils;

import eu.spod.isislab.spodapp.adapters.NewsfeedPostsAdapter;

public interface NewsfeedPostRefreshable {
    void refreshPost(String entityType, int entityId, NewsfeedPostsAdapter.AdapterUpdateType updateType);
}
