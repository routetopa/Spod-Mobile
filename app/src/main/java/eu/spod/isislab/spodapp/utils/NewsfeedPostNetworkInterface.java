package eu.spod.isislab.spodapp.utils;

import eu.spod.isislab.spodapp.adapters.NewsfeedPostsAdapter;

public interface NewsfeedPostNetworkInterface {
    void nRequestAuthorization();

    void nLoadFeedPage(boolean resetList);

    void nRefreshPost(String entityType, int entityId, NewsfeedPostsAdapter.AdapterUpdateType updateType);

    void nSendPost(String message, byte[] attachment, String filename);

    void nLikeUnlikePost(int position);

    void nDeletePost(int position);

    void nFlagContent(int position, String reason);

    void nStopPendingRequest();
}
