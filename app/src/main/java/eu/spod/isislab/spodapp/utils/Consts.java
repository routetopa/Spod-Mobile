package eu.spod.isislab.spodapp.utils;

public class Consts
{
    public static final int INTERNET_PERMISSION = 0;
    public static final int ACCESS_COARSE_LOCATION_PERMISSION = 1;
    public static final int WRITE_EXTERNAL_STORAGE_PERMISSION = 2;
    public static final int READ_EXTERNAL_STORAGE_PERMISSION  = 3;
    public static final int ASK_APPLICAITON_PERMISSIONS = 4;

    public static final String COCREATION_PLUGIN                    = "cocreation";
    public static final String COCREATION_ACTION_NEW_ROOM           = COCREATION_PLUGIN + "_new_room";
    public static final String COCREATION_ACTION_COMMENT            = COCREATION_PLUGIN + "_comment";
    public static final String COCREATION_ACTION_INVITE             = COCREATION_PLUGIN + "_invite";
    public static final String COCREATION_ACTION_DATASET_PUBLISHED  = COCREATION_PLUGIN + "_dataset_published";
    public static final String AGORA_PLUGIN                         = "agora";
    public static final String AGORA_ACTION_NEW_ROOM                = AGORA_PLUGIN + "_new_room";
    public static final String AGORA_ACTION_COMMENT                 = AGORA_PLUGIN + "_add_comment";
    public static final String AGORA_ACTION_MENTION                 = AGORA_PLUGIN + "_mention";
    public static final String AGORA_ACTION_REPLAY                  = AGORA_PLUGIN + "_replay";

    public static final String SPOD_MOBILE_PREFERENCES    = "eu.spod.isislab.spodapp.preferences.preferences";
    public static final String SPOD_ENDPOINT_PREFERENCES  = "eu.spod.isislab.spodapp.preferences.spod_endpoint";
    public static final String USED_INTENT                = "eu.spod.isislab.spodapp.preferences.USED_INTENT";

    public static final String SHARED_PREF_FIREBASE_TOKEN = "eu.spod.isislab.spodapp.services.SpodFirebaseInstanceIdService.firebaseToken";

    //Network channel consts
    public static final String SERVICE_LOGIN                        = "SERVICE_LOGIN";
    public static final String SERVICE_GET_USER_INFO                = "SERVICE_GET_USER_INFO";
    public static final String SERVICE_AGORA_GET_COMMENTS           = "SERVICE_AGORA_GET_COMMENTS";
    public static final String SERVICE_AGORA_GET_ROOMS              = "SERVICE_AGORA_GET_ROOMS";
    public static final String SERVICE_AGORA_GET_PAGED_COMMENTS     = "SERVICE_AGORA_GET_PAGED_COMMENTS";
    public static final String SERVICE_AGORA_ADD_COMMENT            = "SERVICE_AGORA_ADD_COMMENT";
    public static final String SERVICE_COCREATION_GET_SHEET_DATA    = "SERVICE_COCREATION_GET_SHEET_DATA";
    public static final String SERVICE_COCREATION_GET_ROOMS         = "SERVICE_COCREATION_GET_ROOMS";
    public static final String SERVICE_COCREATION_GET_METADATA      = "SERVICE_COCREATION_GET_METADATA";
    public static final String SERVICE_COCREATION_GET_DATALETS      = "SERVICE_COCREATION_GET_DATALETS";
    public static final String SERVICE_COCREATION_GET_COMMENTS      = "SERVICE_COCREATION_GET_DISCUSSION";
    public static final String SERVICE_COCREATION_ADD_COMMENT       = "SERVICE_COCREATION_ADD_COMMENT";
    public static final String SERVICE_COCREATION_JOIN_ROOM         = "SERVICE_COCREATION_JOIN_ROOM";
    public static final String SERVICE_COCREATION_GET_ALL_FRIENDS   = "SERVICE_COCREATION_GET_ALL_FRIENDS";
    public static final String SERVICE_COCREATION_INVITE_FRIENDS    = "SERVICE_COCREATION_INVITE_FRIENDS";
    public static final String SERVICE_SYNC_NOTIFICATION            = "SERVICE_SYNC_NOTIFICATION";
    public static final String SERVICE_SAVE_NOTIFICATION            = "SERVICE_SAVE_NOTIFICATION";

    public static final String RESET_PASSWORD_URL                   = "/oauth2/password/reset";
    public static final String CREATE_ACCOUNT_URL                   = "/oauth2/register";
    public static final String LOGOUT_URL                           = "/login";
    public static final String COCREATION_DATASET_ENDPOINT          = "/ethersheet/s/";
    public static final String COCREATION_DOCUMENT_ENDPOINT         = "/etherpad/p/";
    public static final String DEEP_ENDPOINT                        = "http://deep.routetopa.eu/deep_1_19";

    public static String SPOD_ENDPOINT                             = "";
    //private static final String POST_LOGIN_HANDLER                  = "/base/user/ajax-sign-in/";//"/openid/ajax.php";;
    //Coreation
    public static final String GET_USER_INFO                       = "/cocreation/ajax/get-user-info/";
    public static final String MEDIAROOM_ADD_NEW_ROW               = "/ethersheet/mediaroom/addrow/";
    public static final String COCREATION_CREATE_ROOM              = "/cocreation/ajax/create-media-room-from-mobile/";
    public static final String GET_COCREATION_ROOMS                = "/cocreation/ajax/get-cocreation-rooms-by-user-id/";
    public static final String GET_COCREATION_ROOM_METADATA        = "/cocreation/ajax/get-metadata-by-room-id/";
    public static final String GET_COCREATION_ROOM_DATALETS        = "/cocreation/ajax/get-datalets-by-room-id/";
    public static final String COCREATION_ROOM_JOIN_ROOM           = "/cocreation/ajax/confirm-to-join-to-room/";
    //public static final String GET_COCREATION_MEDIA_ROOMS          = "/cocreation/ajax/get-media-rooms-by-user-id/";
    public static final String GET_COCREATION_ROOMS_SHEET_DATA     = "/cocreation/ajax/get-sheet-data-by-room-id/";
    public static final String GET_COCREATION_ROOM_COMMENTS        = "/spod_plugin_discussion/ajax/get-comments/";
    public static final String COCREATION_ROOM_ADD_COMMENT         = "/spod_plugin_discussion/ajax/add-comment/";
    public static final String COCREATION_ROOM_GET_ALL_FRIENDS     = "/cocreation/ajax/get-all-friends/";
    public static final String COCREATION_ROOM_INVITE_FRIENDS      = "/cocreation/ajax/add-new-members-to-room-from-mobile/";
    //Agora
    public static final String GET_AGORA_ROOMS                     = "/agora/ajax/get-rooms";
    public static final String AGORA_ADD_ROOM                      = "/agora/ajax/add-agora-room";
    public static final String GET_AGORA_ROOM_COMMENTS             = "/agora/ajax/get-comments-page/";
    public static final String AGORA_ROOM_ADD_COMMENTS             = "/agora/ajax/add-comment/";
    public static final String AGORA_ROOM_GET_NESTED_COMMENTS      = "/agora/ajax/get-nested-comment-json/";
    public static final String DATALET_STATIC_IMAGE_URL            = "/ow_plugins/ode/datalet_images/datalet_#.png";
    public static final String DATALET_STATIC_URL                  = "/share_datalet/#";
    //Sync notification
    public static final String SYNC_NOTIFICATION_ENDPOINT             = "/realtime_notification";
    public static final String COCREATION_SYNC_NOTIFICATION_ENDPOINT  = "/ethersheet/#/pubsub/";
    //Firebase Notification
    public static final String FIREBASE_REGISTRATION_ID_ENDPOINT      = "/notification_system/ajax/add-user-registration-id/";
    //Settings
    public static final String SAVE_MOBILE_NOTIFICATION               = "/notification_system/ajax/register-user-for-action/";

}
