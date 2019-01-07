import json
import os
import http.client


def is_valid_app_id(connection, ID):
    try:
        data = get_json(connection, "/appreviews/" + str(ID) + "?json=1")
        result = data["success"] == 1
    except:
        result = False
    return result

    
def get_json(connection, url):
    connection.request("GET", url)
    response = connection.getresponse()
    data = response.read()
    return json.loads(data.decode("utf-8"))


def get_app_list():
    connection = http.client.HTTPSConnection("api.steampowered.com")
    connection.request("GET", "/ISteamApps/GetAppList/v2/")
    response = connection.getresponse()
    data = response.read()
    connection.close()
    return json.loads(data.decode("utf-8"))["applist"]["apps"]


def get_max_ID(app_list):
    return max(list(map(lambda entry: entry['appid'], app_list)))


def get_game_title(ID, app_list):
    try:
        title = list(filter(lambda entry: entry['appid'] == ID, app_list))[0]
    except IndexError:
        title = {"appid": ID, "name": ""}
    return title


def get_specific_reviews_for_app(connection, ID, review_type, review_limit):
    review_list = []
    review_offset = 0
    get_more_reviews = True

    # causes early request stop, but sends one more request than actually necessary
    while get_more_reviews:
        # day range 9223372036854775807 is a trick
        url = "/appreviews/" + str(ID) + "?json=1" + \
              "&filter=all" + \
              "&language=all" + \
              "&day_range=9223372036854775807" + \
              "&start_offset=" + str(review_offset) + \
              "&review_type=" + review_type + \
              "&purchase_type=all" + \
              "&num_per_page=100"

        print("Game ID: {} --- {}".format(ID, review_offset))
        json_data = get_json(connection, url)
        for review in json_data["reviews"]:
            if review["language"] == "english" and len(review_list) < review_limit:
                review_list.append(review)

        review_offset += 100
        get_more_reviews = json_data["query_summary"]["num_reviews"] > 0 and len(review_list) < review_limit

    return review_list


# exclusively english reviews that have at least 1 up-vote are fetched
def get_reviews_for_app(connection, ID):
    reviews = get_specific_reviews_for_app(connection, ID, review_type="all", review_limit=10000000)
    return reviews


def get_number_of_positive_reviews(reviews):
    count = 0
    mapped_reviews = list(map(lambda review: int(review["voted_up"]), reviews))
    for x in mapped_reviews:
        count += x
    return count


def create_json_entry(connection, app_ID, app_list):
    app_title = get_game_title(app_ID, app_list)["name"]
    reviews = get_reviews_for_app(connection, app_ID)

    total_reviews = len(reviews)
    total_pos_reviews = get_number_of_positive_reviews(reviews)
    total_neg_reviews = total_reviews - total_pos_reviews

    print("Total number of reviews: {}".format(total_reviews))
    print("Positive: {} --- Negative: {}".format(total_pos_reviews, total_neg_reviews))
    app_data = {"app_id": app_ID,
                "app_title": app_title,
                "total_reviews": total_reviews,
                "total_positive": total_pos_reviews,
                "total_negative": total_neg_reviews,
                "reviews": reviews}
    return app_data


def write_data_as_json(data, file_path, indent=None):
    with open(file_path, "w", encoding="utf-8") as out_file:
        json.dump(data, out_file, indent=indent)


def get_steam_data(start_ID, indent=None):

    app_list = get_app_list()
    connection = http.client.HTTPSConnection("store.steampowered.com")

    app_id = start_ID
    app_id_max = get_max_ID(app_list)

    path = "steam_review_data"
    if not os.path.exists(path):
        os.makedirs(path)

    while app_id <= app_id_max:

        if is_valid_app_id(connection, app_id):

            print(get_game_title(app_id, app_list))
            file_path = os.path.join(path, "App_" + str(app_id) + ".json")
            json_entry = create_json_entry(connection, app_id, app_list)
            print("-------------------------------------\n")

            if len(json_entry["reviews"]) > 0:
                write_data_as_json(json_entry, file_path, indent)

        app_id += 1

    connection.close()


def main():
    get_steam_data(start_ID=99910, indent=0)

        
if __name__ == "__main__":
    main()
