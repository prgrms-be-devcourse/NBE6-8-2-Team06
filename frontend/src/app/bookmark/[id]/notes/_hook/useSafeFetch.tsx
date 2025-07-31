import { useRouter } from "next/navigation";

export const useSafeFetch = () => {
    const router = useRouter();

    const safeFetch = async (input: RequestInfo, init?: RequestInit) => {
        const res = await fetch(input, {
            ...init,
            credentials: "include",
        });

        const json = await res.json();
        console.log(json)
        if (res.status !== 200) {
            alert(`${json.resultCode}: ${json.msg}`);
            router.push("/");
            throw new Error("권한 없음");
        }

        return json;
    };

    return { safeFetch };
};