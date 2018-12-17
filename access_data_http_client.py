import json
import http.client


def main():
    data = get_steam_data()
    write_data_as_json(data, "review_data2.json", indent=4)


def is_valid_app_id(connection, ID):
    data = get_json(connection, "/appreviews/" + str(ID) + "?json=1")
    return data["success"] == 1

    
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
    return list(filter(lambda entry: entry['appid'] == ID, app_list))[0]


def get_all_reviews_for_app(connection, ID):

    review_list = []
    review_offset = 0
    review_limit_per_app = 1500  # multiple of num_per_page
    more_reviews_available = True

    # causes early request stop, but sends one more request than actually necessary -> might be optimized
    while more_reviews_available:
        # day range 9223372036854775807 might be a hack
        url = "/appreviews/" + str(ID) + "?json=1" + \
              "&filter=all" + \
              "&language=all" + \
              "&day_range=9223372036854775807" + \
              "&start_offset=" + str(review_offset) + \
              "&review_type=all" + \
              "&purchase_type=all" + \
              "&num_per_page=100"

        print("Game ID: {} --- {}".format(ID, review_offset))
        json_data = get_json(connection, url)
        more_reviews_available = json_data["query_summary"]["num_reviews"] > 0
        for review in json_data["reviews"]:
            review_list.append(review)

        review_offset += 100

    return review_list


def get_number_of_positive_reviews(reviews):
    count = 0
    mapped_reviews = list(map(lambda review: int(review["voted_up"]), reviews))
    for x in mapped_reviews:
        count += x
    return count


def create_json_entry(connection, app_ID, app_list):
    app_title = get_game_title(app_ID, app_list)["name"]
    reviews = get_all_reviews_for_app(connection, app_ID)

    total_reviews = len(reviews)
    total_pos_reviews = get_number_of_positive_reviews(reviews)
    total_neg_reviews = total_reviews - total_pos_reviews

    print("Total number of reviews: {}".format(total_reviews))
    app_data = {"app_id": app_ID,
                "app_title": app_title,
                "total_reviews": total_reviews,
                "total_positive": total_pos_reviews,
                "total_negative": total_neg_reviews,
                "reviews": reviews}
    return app_data


def get_steam_data():

    app_list = get_app_list()
    connection = http.client.HTTPSConnection("store.steampowered.com")

    data = []
    app_id = 0
    app_id_max = get_max_ID(app_list)

    while app_id <= app_id_max:

        if not is_valid_app_id(connection, app_id):
            app_id += 1

        else:
            print(get_game_title(app_id, app_list))
            data.append(create_json_entry(connection, app_id, app_list))
            print("-------------------------------------\n")

        app_id += 1

    connection.close()
    return {"steam_apps": data}


def write_data_as_json(data, file_path, indent=None):
    with open(file_path, "w", encoding="utf-8") as out_file:
        json.dump(data, out_file, indent=indent)


def create_csv_entry(json_entry):
    pass


def create_csv_file(json_data):
    pass

        
if __name__ == "__main__":
    main()
