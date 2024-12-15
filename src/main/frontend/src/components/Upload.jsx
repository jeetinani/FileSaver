import axios, { HttpStatusCode } from "axios";
import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Retreive from "./Retreive";
import { useLocation } from "react-router-dom";



export default function Upload() {

    const [alert, setAlert] = useState("");
    const [downloadLink, setDownloadLink] = useState("");
    const [downloadHere, setdownloadHere] = useState("");


    function useQuery() {
        return new URLSearchParams(useLocation().search);
    }
    const query = useQuery();
    const uuid = query.get("uuid");



    const uploadHandler = (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);
        axios.post("/upload", formData, {
            headers: {
                "Content-Type": "multipart/form-data",
            },
        })
            .then(resp => {
                setAlert("");
                setDownloadLink(resp.data.retrievePath);
                setdownloadHere("Download link : "+resp.data.retrievePath);
            }).catch((error) => {
                //console.log(JSON.stringify(error));
                console.error("Login failed:", error);
                if (error.response.status === HttpStatusCode.BadRequest) {
                    setAlert("File too large");
                }else{
                    setAlert("Failed");
                }
            });
    }
    return (
        <>
            {uuid ? <Retreive uuid={uuid} /> :
                <div className="row">
                    <div className="col:6 mx-auto">
                        <h3>Upload</h3>
                        <h6>Max file size limit is 10MB</h6>
                        <form onSubmit={uploadHandler}>
                            <div className="mb-3">
                                <label htmlFor="file" className="form-label">File</label>
                                <input type="file" className="form-control" id="file"
                                    name="file" required placeholder="File" />
                            </div>
                            <div className="mb-3">
                                <label htmlFor="passcode" className="form-label">Passcode</label>
                                <input type="text" className="form-control" id="passcode"
                                    name="passcode" required placeholder="Passcode" />
                            </div>
                            <div className="mb-3">
                                <button type="submit" className="btn btn-success" name="upload">Upload</button>
                            </div>
                            <div>
                                {alert}
                            </div>
                            <div>
                                <a href={downloadLink} target="_blank" rel="noopener noreferrer">{downloadHere}</a>
                            </div>
                        </form>
                    </div>
                </div>
            }
        </>
    )
}